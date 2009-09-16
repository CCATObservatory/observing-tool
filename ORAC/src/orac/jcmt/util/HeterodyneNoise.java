package orac.jcmt.util ;

import java.io.BufferedReader ;
import java.io.InputStreamReader ;
import java.io.InputStream ;
import java.net.URL ;
import java.util.Iterator ;
import java.util.Set ;
import java.util.StringTokenizer ;
import java.util.TreeMap ;
import java.util.Vector ;

import orac.jcmt.iter.SpIterJCMTObs ;
import orac.jcmt.iter.SpIterRasterObs ;
import orac.jcmt.inst.SpInstHeterodyne ;
import orac.jcmt.iter.SpIterJiggleObs ;
import orac.jcmt.iter.SpIterStareObs ;

import gemini.util.MathUtil ;
import gemini.util.ObservingToolUtilities ;

/**
 * Class for calculation of the system noise of the heterodyne receivers.
 * This class is based on the perl model JCMT::HITEC which is comprised of
 * code used to create the integration time calculator 
 */
public class HeterodyneNoise
{
	static Vector<String> feNames = new Vector<String>() ;
	static Vector<TreeMap<Double,Double>> trxValues = new Vector<TreeMap<Double,Double>>() ;
	static Vector<Double> nu_tel = new Vector<Double>() ;
	static TreeMap<Double,String> _availableBands = new TreeMap<Double,String>() ;
	static final double kappa = 1.15 ;
	static String cfgDir = System.getProperty( "ot.cfgdir" ) ;
	private static final String receiverFile = "receiver.info" ;
	private static boolean initialised = false ;
	private static int SUBSYSTEM = 0 ;

	private static void init()
	{
		String inputLine ;

		if( feNames.size() != 0 )
			return ;

		// Read the reciever file - first get its directory and add the file name
		if( !cfgDir.endsWith( "/" ) )
			cfgDir += '/' ;
		String fileName = cfgDir + receiverFile ;
		// Open the file ready for reading
		URL url = ObservingToolUtilities.resourceURL( fileName ) ;
		try
		{
			InputStream is = url.openStream() ;
			BufferedReader in = new BufferedReader( new InputStreamReader( is ) ) ;
			// Read the front end names
			while( ( inputLine = in.readLine() ) != null )
			{
				if( inputLine.equals( "" ) )
					break ;
				feNames.add( inputLine ) ;
			}
			// Now loop through the rest of the file and get the receiver temperature
			int index ;
			while( ( inputLine = in.readLine() ) != null )
			{
				if( ( index = feNames.indexOf( inputLine ) ) != -1 )
				{
					// Start reading the data
					TreeMap<Double,Double> currentMap = new TreeMap<Double,Double>() ;
					int nLines = Integer.valueOf( in.readLine() ) ;
					for( int i = 0 ; i < nLines ; i++ )
					{
						String values = in.readLine() ;
						StringTokenizer st = new StringTokenizer( values ) ;
						Double frequency = new Double( st.nextToken() ) ;
						Double trx = new Double( st.nextToken() ) ;
						currentMap.put( frequency , trx ) ;
					}
					trxValues.add( index , currentMap ) ;
					Double nu = new Double( in.readLine() ) ;
					nu_tel.add( index , nu ) ;
				}
			}
			in.close() ;
			is.close() ;
		}
		catch( Exception e )
		{
			System.out.println( "Error reading receiver info file" ) ;
			e.printStackTrace() ;
		}

		getAvailableTau() ;

		initialised = true ;
	}

	private static void getAvailableTau()
	{
		String tauList = cfgDir + "tau.list" ;
		URL url = ObservingToolUtilities.resourceURL( tauList ) ;
		Vector<String> filesVector = new Vector<String>() ;
		if( url != null )
		{
			try
			{
				InputStream is = url.openStream() ;
				BufferedReader in = new BufferedReader( new InputStreamReader( is ) ) ;
				String inputLine = "" ;
				while( ( inputLine = in.readLine() ) != null )
					filesVector.add( inputLine ) ;
				in.close() ;
				is.close() ;
			}
			catch( Exception e )
			{
				System.out.println( "Problems reading file " + tauList + " : " + e ) ;
			}
			String[] files = new String[ filesVector.size() ] ;
			filesVector.toArray( files ) ;
			if( files != null )
			{
				for( int i = 0 ; i < files.length ; i++ )
				{
					if( files[ i ].startsWith( "tau" ) && files[ i ].endsWith( ".dat" ) )
					{
						// Extract the tau value
						String value = "0." + files[ i ].substring( files[ i ].indexOf( "u" ) + 1 , files[ i ].lastIndexOf( "." ) ) ;
						double dtmp = Double.parseDouble( value ) ;
						_availableBands.put( new Double( dtmp ) , files[ i ] ) ;
					}
				}
			}
			else
			{
				System.out.println( "No files in " + cfgDir ) ;
			}
		}
	}

	public static double getTrx( String fe , double freq )
	{
		double trx = 0. ;
		int index ;

		/* Now see if this is in our available list of front ends */
		if( ( index = feNames.indexOf( fe ) ) >= 0 )
		{
			// There is a match, now extract the appropriate tree map
			TreeMap<Double,Double> thisMap = trxValues.elementAt( index ) ;
			if( thisMap.size() > 1 )
			{
				// first make sure it is within range or short circuit now
				Double nextTrxFrequency = thisMap.firstKey() ;
				if( freq < nextTrxFrequency )
					trx = thisMap.get( nextTrxFrequency ) ;

				Double lastTrxFrequency = thisMap.lastKey() ;
				if( freq > lastTrxFrequency )
					trx = thisMap.get( lastTrxFrequency ) ;

				if( trx == 0 )
				{
					/*
					 * We are going to do a linear interpolation between the
					 * surrounding frequency values, so we need to find the
					 * values at the appropriate keys
					 */
					Set<Double> set = thisMap.keySet() ;
					Iterator<Double> iter = set.iterator() ;
					while( iter.hasNext() )
					{

						lastTrxFrequency = nextTrxFrequency ;
						nextTrxFrequency = iter.next() ;
						if( freq < nextTrxFrequency && freq >= lastTrxFrequency )
							break ; // We have the info we need
					}

					try
					{
						// Now get the corresponding Trx values
						Double lastTrxValue = thisMap.get( lastTrxFrequency ) ;
						Double nextTrxValue = thisMap.get( nextTrxFrequency ) ;
						// Now interpolate...
						trx = linterp( lastTrxFrequency , lastTrxValue , nextTrxFrequency , nextTrxValue , freq ) ;
					}
					catch( NullPointerException npe )
					{
						throw new RuntimeException( "Heterodyne not initialised" ) ;
					}
				}
			}
			else
			{
				trx = thisMap.get( thisMap.firstKey() ) ;
			}
		}
		return trx ;
	}

	public static double getTsys
	( 
			String fe , // The front end name
			double tau , // The noise calculation tau
			double airmass , // The airmass to use
			double freq , // Required frequency
			boolean ssb // Whether or not we are using SSB of DSB
	)
	{
		int index ;
		double nuTel = 0. ;
		double tSys = 0. ;

		if( !initialised )
			init() ;

		// Find which tau range contains the required tau, and the next tau range nearest
		String tauFile0 ;
		String tauFile1 ;
		Iterator<Double> iter = _availableBands.keySet().iterator() ;
		double current = 0 , next = 0 ;
		boolean firstLoop = true ;
		while( iter.hasNext() )
		{
			if( firstLoop )
			{
				current = iter.next() ;
				next = iter.next() ;
				if( tau <= current )
					break ;
				firstLoop = false ;
				continue ;
			}
			current = next ;
			next = iter.next().doubleValue() ;
			if( tau >= current && tau < next )
				break ;
		}
		tauFile0 = _availableBands.get( new Double( current ) ) ;
		tauFile1 = _availableBands.get( new Double( next ) ) ;

		double tranmission0 = getTransmission( tauFile0 , freq ) ;
		double tranmission1 = getTransmission( tauFile1 , freq ) ;
		double t = linterp( current , tranmission0 , next , tranmission1 , tau ) ;

		if( ( index = feNames.indexOf( fe ) ) != -1 )
			nuTel = nu_tel.elementAt( index ) ;

		// Nowe find Tsys
		double nuSky = Math.exp( -t * airmass ) ;
		double tSky = 260. - nuSky * 260. ;
		double tTel = 265. - nuTel * 265. ;

		if( ssb )
		{
			if( "HARP".equals( fe ) || "WD".equals( fe ) )
				tSys = ( getTrx( fe , freq ) + nuTel * tSky + tTel ) / ( nuSky * nuTel ) ;
			else
				tSys = ( 2. * getTrx( fe , freq ) + nuTel * tSky + tTel ) / ( nuSky * nuTel ) ;
		}
		else
		{
			tSys = 2 * ( getTrx( fe , freq ) + nuTel * tSky + tTel ) / ( nuSky * nuTel ) ;
		}

		return tSys ;
	}

	private static TreeMap<Double,Double> getAtmosphereData( double tau )
	{
		double[] availableBands = { 0.03 , 0.05 , 0.065 , 0.1 , 0.16 , 0.2 } ;
		TreeMap<Double,Double> tauMap = new TreeMap<Double,Double>() ;
		int index = 0 ;

		// Find the tau band we are in
		if( tau >= availableBands[ availableBands.length - 1 ] )
		{
			index = availableBands.length - 1 ;
		}
		else if( tau < availableBands[ 0 ] )
		{
			index = 0 ;
		}
		else
		{
			for( int i = 1 ; i < availableBands.length ; i++ )
			{
				if( tau >= availableBands[ i - 1 ] && tau < availableBands[ i ] )
					break ;
				index++ ;
			}
		}

		// We now have the index so open the file
		String fileName = cfgDir ;
		if( !fileName.endsWith( "/" ) )
			fileName += '/' ;
		fileName += "tau" ;
		String tmp = Double.toString( availableBands[ index ] ) ;
		StringTokenizer st = new StringTokenizer( tmp , "." ) ;
		st.nextToken() ;
		tmp = st.nextToken() + ".dat" ;
		fileName = fileName + tmp ;
		URL url = ObservingToolUtilities.resourceURL( fileName ) ;
		try
		{
			InputStream is = url.openStream();
			BufferedReader fileReader = new BufferedReader( new InputStreamReader( is ) ) ;
			String inputLine ;
			while( ( inputLine = fileReader.readLine() ) != null )
			{
				st = new StringTokenizer( inputLine ) ;
				Double frequency = new Double( st.nextToken() ) ;
				Double depth = new Double( st.nextToken() ) ;
				tauMap.put( frequency , depth ) ;
			}
			fileReader.close() ;
			is.close() ;
		}
		catch( Exception e )
		{
			e.printStackTrace() ;
		}
		return tauMap ;
	}

	private static double getTransmission( String filename , double freq )
	{
		String path = cfgDir ;
		if( !path.endsWith( "/" ) )
			path += '/' ;
		path += filename ;
		double rtn = 0. ;
		BufferedReader rdr = null ;
		URL url = ObservingToolUtilities.resourceURL( path ) ;
		try
		{
			InputStream is = url.openStream() ;
			rdr = new BufferedReader( new InputStreamReader( is ) ) ;
			// Read the first line of the file
			String line = rdr.readLine() ;
			double freq1 = 0. , tran1 = 0. ;
			double freq2 = 0. , tran2 = 0. ;
			if( line != null )
			{
				StringTokenizer st = new StringTokenizer( line ) ;
				freq1 = Double.parseDouble( st.nextToken() ) ;
				tran1 = Double.parseDouble( st.nextToken() ) ;
			}
			// Now loop over all the values til we find the ones we want
			while( ( line = rdr.readLine() ) != null )
			{
				StringTokenizer st = new StringTokenizer( line ) ;
				freq2 = Double.parseDouble( st.nextToken() ) ;
				tran2 = Double.parseDouble( st.nextToken() ) ;
				// First deal with the case where the freq is below the first value
				if( freq < freq1 )
					break ;
				// See is the frequency we need is between the two we have
				if( freq >= freq1 && freq < freq2 )
					break ;
				// Otherwise, keep looping
				freq1 = freq2 ;
				tran1 = tran2 ;
			}

			// Once we get here, we should be able to interpolate
			rtn = linterp( freq1 , tran1 , freq2 , tran2 , freq ) ;
			rdr.close() ;
			is.close() ;
		}
		catch( java.io.FileNotFoundException fnf )
		{
			System.out.println( "Unable to find file " + path ) ;
		}
		catch( java.io.IOException ioe )
		{
			System.out.println( "IO Error while reading file " + path ) ;
		}
		finally
		{
			if( rdr != null )
			{
				try
				{
					rdr.close() ;
				}
				catch( Exception e ){}
			}
		}

		return rtn ;

	}

	public static double linterp( double x1 , double y1 , double x2 , double y2 , double x )
	{
		double slope = ( y2 - y1 ) / ( x2 - x1 ) ;
		double value = slope * x + y1 - slope * x1 ;
		return value ;
	}

	private static double getNoise( SpIterJCMTObs obs , SpInstHeterodyne inst , double tSys )
	{
		double time = 0. ;
		double np = 1. ;
		int shared = 0 ;
		int samplesPerRow = 1 ;
	
		double multiscan = 1. ;

		if( obs instanceof SpIterRasterObs )
		{
			// Make this the total time to do the map
			SpIterRasterObs rasterObs = ( SpIterRasterObs )obs ;
			double sampleTime = rasterObs.getSampleTime() ;
			samplesPerRow = ( int )rasterObs.numberOfSamplesPerRow() ;

			time = sampleTime ;

			np = samplesPerRow ;
			
			if( inst.getFrontEnd().equals( "HARP" ) )
			{
				double scale = rasterObs.getScanDy() / SpIterRasterObs.HARP_FULL_ARRAY ;
				multiscan = Math.sqrt( scale ) ;
			}
			
			shared = 1 ;
		}
		else if( obs instanceof SpIterJiggleObs )
		{
			SpIterJiggleObs jiggleObs = ( SpIterJiggleObs )obs ;
			time = ( double )jiggleObs.getSecsPerCycle() ;
			np = ( ( SpIterJiggleObs )obs ).getNumberOfPoints() ;
			shared = jiggleObs.hasSeparateOffs() ? 0 : 1 ;
		}
		else if( obs instanceof SpIterStareObs )
		{
			SpIterStareObs stareObs = ( SpIterStareObs )obs ;
			shared = stareObs.hasSeparateOffs() ? 0 : 1 ;
			time = ( double )stareObs.getSecsPerCycle() ;
		}
		else
		{
			time = ( double )obs.getSecsPerCycle() ;
		}

		double bandwidth = inst.getBandWidth( SUBSYSTEM ) ;
		int channels = inst.getChannels( SUBSYSTEM ) ;
		int resolution = ( int )Math.rint( ( bandwidth ) / channels ) ;

		// Multiply by the number of mixers
		if( inst.getMixer().startsWith( "Dual" ) )
		{
			time *= 2. ;
			resolution *= 2 ;
		}

		double factor = ( shared * Math.sqrt( 1 + ( 1 / Math.sqrt( np ) ) ) ) + ( ( 1 - shared ) * ( Math.sqrt( 2 ) ) ) ;
		double rmsNoise = 1.04 * factor * tSys * 1.23 / Math.sqrt( resolution * time ) ;
		rmsNoise *= multiscan ;
		return MathUtil.round( rmsNoise , 3 ) ;
	}
	
	public static double getHeterodyneNoise( SpIterJCMTObs obs , SpInstHeterodyne instrument , double tSys )
	{
		// Set up all the vectors required
		if( !initialised )
			init() ;

		//Now calulate the noise based on this
		double noise = getNoise( obs , instrument , tSys ) ;
		return noise ;
	}

	public static double getHeterodyneNoise( SpIterJCMTObs obs , SpInstHeterodyne instrument , double noiseCalculationTau , double airmass )
	{
		// Set up all the vectors required
		if( !initialised )
			init() ;

		// Get the system temperaure
		double tSys = getTsys( instrument.getFrontEnd() , noiseCalculationTau , airmass , ( instrument.getRestFrequency( 0 ) ) / 1.0e9 , instrument.getMode().equalsIgnoreCase( "ssb" ) ) ;

		//Now calulate the noise based on this
		return getHeterodyneNoise( obs , instrument , tSys ) ;
	}

	public static void main( String[] args ){}
}
