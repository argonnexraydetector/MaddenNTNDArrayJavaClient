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
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVInt;


import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.Type;
 


import org.epics.nt.*;

public class imageget {
	public PvaClient pva;
	public PvaClientChannel mychannel;
	public PVStructure read_request;
	public PvaClientMonitor pvamon;
	public PvaClientMonitorData easydata;
	public Convert converter;
	
	public imageget(String channame)
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
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		System.out.println("Maddens test java get image");
		
		imageget mygetter = new imageget("13SIM1:Pva1:Image");
		boolean isrunning = true;
		
		while(isrunning)
		{
			mygetter.pvamon.waitEvent(0);
			mygetter.easydata = mygetter.pvamon.getData();		 		
					
			PVStructure pvs = mygetter.easydata.getPVStructure();				

		//!! why does wrap return null? it should work?
		NTNDArray myarray =NTNDArray.wrapUnsafe(pvs);
		//The wrap unsafe leaves out most of the NTNDArray fields. but wrap() feils and returns null.
		//	
		
		int uniqueid  =mygetter.getUniqueId(myarray);
		int ndims = mygetter.getNumDims(myarray);
		
		// can bu size, binning, or whatever in dims
		int dimsint[]=mygetter.getDimsInfo(myarray, "size");
		
		String dimstring = mygetter.getDimsString(myarray);
		//returns like ushort[]		
		
		PVScalarArray imagedata = mygetter.extractImageData( myarray);

		String arraytype = mygetter.getImageDataType( imagedata);
		int arraylen = mygetter.getImageLength(imagedata);
			
				
		int[] pixels = new int[arraylen];
			
		switch (arraytype)
		{
		case "short[]":
		case "ushort[]":
			//toIntArray(PvScalarrarray, offset, len, int[], tooffset);

			mygetter.converter.toIntArray(imagedata, 0, arraylen, pixels, 0);
		break;
		}
		System.out.println("got data " + arraytype 
				+ "\n    len="+arraylen
				+ "\n    uniqueid=" + uniqueid
				+ "\n" + dimstring);



		mygetter.pvamon.releaseEvent();
		

		
		}//while true
	
		System.out.println("Maddens test java end");

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
