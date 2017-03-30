package epicsv4test;

import org.epics.pvaClient.PvaClient;
import org.epics.pvaClient.PvaClientChannel;
import org.epics.pvaClient.PvaClientGet;
import org.epics.pvaClient.PvaClientGetData;
import org.epics.pvaClient.PvaClientMonitor;
import org.epics.pvaClient.PvaClientMonitorData;
import org.epics.pvaccess.ClientFactory;
import org.epics.pvaccess.client.ChannelProviderRegistry;
import org.epics.pvdata.pv.PVScalarArray;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.StructureArrayData;
import org.epics.pvdata.pv.LongArrayData;
import org.epics.pvdata.copy.CreateRequest;
import org.epics.pvdata.factory.BasePVUByteArray;
import org.epics.pvdata.factory.ConvertFactory;
import org.epics.pvdata.pv.ByteArrayData;
import org.epics.pvdata.pv.Convert;
import org.epics.pvdata.pv.PVLongArray;
import org.epics.pvdata.pv.PVScalar;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVStructureArray;
import org.epics.pvdata.pv.PVUByteArray;
import org.epics.pvdata.pv.PVUnion;
import org.epics.pvdata.pv.PVByteArray;
import org.epics.pvdata.pv.PVDouble;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVInt;


import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.Type;
import org.epics.pvdata.pv.Union;

import java.awt.image.BufferedImage;

import org.epics.nt.*;

public class imageget {
	public PvaClient pva;
	public PvaClientChannel mychannel;
	public PVStructure read_request;
	public PvaClientMonitor pvamon;
	public PvaClientMonitorData easydata;
	public Convert converter;
	
	public boolean is_monrunning;
	
	public imageget()
	{
		
		 is_monrunning=false;
		
	}
	
	
	public void connectPVs(String channame)
	{
		 pva=PvaClient.get();
		 mychannel = pva.channel(channame);
		 read_request = CreateRequest.create().createRequest("field()");
		 pvamon=mychannel.createMonitor(read_request);
		pvamon.start();

		//This almost cirrectly gets the NTNDArray.
		 easydata = pvamon.getData();		
		//Structure strct=easydata.getStructure();
		 converter=ConvertFactory.getConvert();
		
	}
	
	
	public void disconnectPVs()
	{
		
		   pvamon.stop();
	        pvamon.destroy();
	            //read_request.destroy();
	           mychannel.getChannel().destroy();
	           mychannel.destroy();
	           //pva.destroy();
	           
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		System.out.println("Maddens test java get image");
		
		imageget mygetter = new imageget();
		mygetter.connectPVs("13SIM1:Pva1:Image");
		
	
		mygetter.is_monrunning=false;
		//runONCE only
		System.out.println("run mon loop 1st time");
		mygetter.monitorLoop();
		System.out.println("run mon loop 2nd time");
		
		mygetter.monitorLoop();
		

		
		
		System.out.println("Maddens test java end");

	}

	
	public void monitorLoop()
	{
		
		do 
		{
			pvamon.waitEvent(0);
			easydata = pvamon.getData();		 		
					
			PVStructure pvs = easydata.getPVStructure();				

			//!! why does wrap return null? it should work?
			NTNDArray myarray =NTNDArray.wrapUnsafe(pvs);
			//The wrap unsafe leaves out most of the NTNDArray fields. but wrap() feils and returns null.
			//	
			PVStructureArray attribs = myarray.getAttribute();
			
			int nattribs= getNumAttributes(myarray);
			
			int colormode = 0;
			
			if (getAttrType( myarray,"ColorMode","value").equals("int"))			
				 colormode = getAttrValInt( myarray,"ColorMode","value");
			else
				System.out.println("colormode not int!!");
			
		
			
			int uniqueid  =getUniqueId(myarray);
			int ndims = getNumDims(myarray);
			
			// can bu size, binning, or whatever in dims
			int dimsint[]=getDimsInfo(myarray, "size");
			
			String dimstring = getDimsString(myarray);
			//returns like ushort[]		
			
			PVScalarArray imagedata = extractImageData( myarray);
	
			String arraytype = getImageDataType( imagedata);
			int arraylen = getImageLength(imagedata);
				
					
			
			System.out.println("got data " + arraytype 
					+ "\n    len="+arraylen
					+ "\n    uniqueid=" + uniqueid
					+ "\n" + dimstring);
	

				
			if (arraytype.equals("ubyte[]"))
			{
				short[] pixels = new short[arraylen];
				converter.toShortArray(imagedata, 0, arraylen, pixels, 0);
				System.out.println("Data = " + pixels[0] + pixels[1] + pixels[2] + pixels[3] + pixels[4] + pixels[5] + pixels[6] + pixels[7] );
				
				
			}
			else if (arraytype.equals("byte[]"))
			{
				byte[] pixels = new byte[arraylen];
				converter.toByteArray(imagedata, 0, arraylen, pixels, 0);
				System.out.println("Data = " + pixels[0] + pixels[1] + pixels[2] + pixels[3] + pixels[4] + pixels[5] + pixels[6] + pixels[7] );
				
			}			
			else if (arraytype.equals("short[]"))
			{
				short[] pixels = new short[arraylen];
				converter.toShortArray(imagedata, 0, arraylen, pixels, 0);
				System.out.println("Data = " + pixels[0] + pixels[1] + pixels[2] + pixels[3] + pixels[4] + pixels[5] + pixels[6] + pixels[7] );
				
			}
			else if (arraytype.equals("ushort[]"))
			{
				int[] pixels = new int[arraylen];
				converter.toIntArray(imagedata, 0, arraylen, pixels, 0);
				System.out.println("Data = " + pixels[0] + pixels[1] + pixels[2] + pixels[3] + pixels[4] + pixels[5] + pixels[6] + pixels[7] );
				
				
			}
			
			
		
			
				
			
			pvamon.releaseEvent();
			

		
		} while(is_monrunning);
			
	}
	
	
	public int getNumAttributes(NTNDArray myarray)
	{
		int nattribs=myarray.getAttribute().getLength();
		return(nattribs);
		
	}

	
	
	/**
	 * return num of dimensions in the NDArray
	 * @param myarray
	 * @return
	 */
	public int getNumDims(NTNDArray myarray)
	{
		int ndims=myarray.getDimension().getLength();		
		return(ndims);
	}
	
	
	public void printMonDataStruct()
	{

		PVStructure pvs = easydata.getPVStructure();
		String [] fnames = easydata.getStructure().getFieldNames();
		
		for (int m=0;m<fnames.length;m++)
			System.out.println(fnames[m]);
		
	}
	
	public int getUniqueId(NTNDArray myarray)
	{
		int uniqueid  =myarray.getUniqueId().get();
		return(uniqueid);
	}
	
	public String getDimsString(NTNDArray myarray)
	{
		PVStructureArray pvdim = myarray.getDimension();
		String dimstring =pvdim.toString();
		return(dimstring);

	}
	
	
	public int[] getDimsInfo(NTNDArray myarray,String whichinfo)
	{
		
				
		int ndims = this.getNumDims(myarray);
		int dimsint[] = new int[ndims];
		PVStructureArray pvdim = myarray.getDimension();

		StructureArrayData dimdata=new StructureArrayData();
		pvdim.get(0,ndims,dimdata);
		
		for (int kk = 0;kk<ndims;kk++)
		{
			PVField[] dimfields = dimdata.data[kk].getPVFields();
			for (int km = 0;km<dimfields.length;km++)
			{
				String dfname = dimfields[km].getField().getID();
				String dfn2=dimfields[km].getFieldName();
				//System.out.println(dfname + " "+dfn2);
				if (dfn2.equals(whichinfo))
				{
					
					dimsint[kk]=converter.toInt((PVScalar)dimfields[km]);
				}
			}
		}
		
		//int nf = pvdim.getNumberFields();
		
		//String dimstring =pvdim.toString();
		return(dimsint);
	}

	

	public String getAttrType(NTNDArray myarray,String attrname,String attrfield) 
	{
		
		
		int nattr = this.getNumAttributes(myarray);
		String attrval=new String("unknown");
		
		PVStructureArray attr1 = myarray.getAttribute();

		StructureArrayData attr2=new StructureArrayData();
		attr1.get(0,nattr,attr2);
				for (int kk = 0;kk<nattr;kk++)
		{
			
			PVField[] attrfields = attr2.data[kk].getPVFields();
			for (int km = 0;km<attrfields.length;km++)
			{
				String dfn2=attrfields[km].getFieldName();
				if (dfn2.equals("name"))
				{
					
					String aname = converter.toString(((PVString)attrfields[km]));
					
					if (aname.equals(attrname))
					{
						for (int mm = 0;mm<attrfields.length;mm++)
						{
							String dfn3=attrfields[mm].getFieldName();
							if (dfn3.equals(attrfield))
							{
								String t =  attrfields[mm].getField().getType().toString();
								
								if (t.equals("union"))
								{
									PVUnion apvu = (PVUnion)attrfields[mm];
									PVField apvuf = apvu.get();
									String s1 = apvuf.getField().getID();
									
									return(s1);
								
								}
								
								
								
								
								
								
							}								
						}
					}
					
				}								
			}
		}
		
		//int nf = pvdim.getNumberFields();
		
		//String dimstring =pvdim.toString();
		return(attrval);
	}

	

	public int getAttrValInt(NTNDArray myarray,String attrname,String attrfield) 
	{
		
		
		int nattr = this.getNumAttributes(myarray);
		int attrval=0;
		
		PVStructureArray attr1 = myarray.getAttribute();

		StructureArrayData attr2=new StructureArrayData();
		attr1.get(0,nattr,attr2);
				for (int kk = 0;kk<nattr;kk++)
		{
			
			PVField[] attrfields = attr2.data[kk].getPVFields();
			for (int km = 0;km<attrfields.length;km++)
			{
				String dfn2=attrfields[km].getFieldName();
				if (dfn2.equals("name"))
				{
					
					String aname = converter.toString(((PVString)attrfields[km]));
					
					if (aname.equals(attrname))
					{
						for (int mm = 0;mm<attrfields.length;mm++)
						{
							String dfn3=attrfields[mm].getFieldName();
							if (dfn3.equals(attrfield))
							{
								String t =  attrfields[mm].getField().getType().toString();
								
								if (t.equals("union"))
								{
									PVUnion apvu = (PVUnion)attrfields[mm];
									PVField apvuf = apvu.get();
									String s1 = apvuf.getField().getID();
									
									if (s1.equals("int"))
									{
										PVInt atri=(PVInt)apvuf;
										attrval = atri.get();
									}
									else
										System.out.println("Error- Wrong attr type");
								
								}
								
								
								
								
								
								return(attrval);
							}								
						}
					}
					
				}								
			}
		}
		
		//int nf = pvdim.getNumberFields();
		
		//String dimstring =pvdim.toString();
		return(attrval);
	}

	
	public double getAttrValDouble(NTNDArray myarray,String attrname,String attrfield) 
	{
		
		
		int nattr = this.getNumAttributes(myarray);
		double attrval=0.0;
		
		PVStructureArray attr1 = myarray.getAttribute();

		StructureArrayData attr2=new StructureArrayData();
		attr1.get(0,nattr,attr2);
				for (int kk = 0;kk<nattr;kk++)
		{
			
			PVField[] attrfields = attr2.data[kk].getPVFields();
			for (int km = 0;km<attrfields.length;km++)
			{
				String dfn2=attrfields[km].getFieldName();
				if (dfn2.equals("name"))
				{
					
					String aname = converter.toString(((PVString)attrfields[km]));
					
					if (aname.equals(attrname))
					{
						for (int mm = 0;mm<attrfields.length;mm++)
						{
							String dfn3=attrfields[mm].getFieldName();
							if (dfn3.equals(attrfield))
							{
								String t =  attrfields[mm].getField().getType().toString();
								
								if (t.equals("union"))
								{
									PVUnion apvu = (PVUnion)attrfields[mm];
									PVField apvuf = apvu.get();
									String s1 = apvuf.getField().getID();
									
									if (s1.equals("double"))
									{
										PVDouble atri=(PVDouble)apvuf;
										attrval = atri.get();
									}
									else
										System.out.println("Error- Wrong attr type");
								
								}
								
								
								
								
								
								return(attrval);
							}								
						}
					}
					
				}								
			}
		}
		
		//int nf = pvdim.getNumberFields();
		
		//String dimstring =pvdim.toString();
		return(attrval);
	}

	
	
	
	String getImageDataType(PVScalarArray imagedata)
	{

		//So I can get the data. How do I know the Union is holdibng bytes[], floats[] or ints[]?
		// Not sure how to ask NTNDArray the data type
		//PVUnion pvu = myarray.getValue();
		// this code works, but we shorten below...This is all introspective
		//PVField has both data and introspection. 
		//PVField pvuf =myarray.getValue().get();
		
		
		
		//pure; intruspection.
		Field pvuff=imagedata.getField();
		
		//tells its a scalar array
		Type pvufft = pvuff.getType();
		//returns like "scalarArray"
		String pvuffts = pvufft.toString();
		//returns something like 'ushort[]'
		String arraytype  =pvuff.getID();
		return(arraytype);

	}
	
	
	public int getImageLength(PVScalarArray imgdata)
	{
		
		// this code works, but we shorten below...This is all introspective
		//PVField has both data and introspection. 
		int arraylen  =imgdata.getLength();
	
		return(arraylen);
		//So I can get the data. How do I know the Union is holdibng bytes[], floats[] or ints[]?
		// Not sure how to ask NTNDArray the data type
		//PVUnion pvu = myarray.getValue();
		// this code works, but we shorten below...This is all introspective
		//PVField has both data and introspection. 
		//PVField pvuf = pvu.get();	
			//PVScalarArray mydata = (PVScalarArray)pvuf;		
			//int arraylen = mydata.getLength();
		
		

		
		
		
	}
	
	PVScalarArray extractImageData(NTNDArray myarray)
	{

		//So I can get the data. How do I know the Union is holdibng bytes[], floats[] or ints[]?
		// Not sure how to ask NTNDArray the data type
		PVUnion pvu = myarray.getValue();
		// this code works, but we shorten below...This is all introspective
		//PVField has both data and introspection. 
		PVField pvuf = pvu.get();
		return((PVScalarArray)pvuf);
	}
				
	
}
