/* $Id: OggPlayer.java,v 1.2 2004/03/04 22:37:01 root777 Exp $ */
/***************************************************************************
 *                      (C) Copyright 2003 - Marauroa                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/

package the1001.client;


import com.jcraft.jogg.Packet;
import com.jcraft.jogg.Page;
import com.jcraft.jogg.StreamState;
import com.jcraft.jogg.SyncState;
import com.jcraft.jorbis.Block;
import com.jcraft.jorbis.Comment;
import com.jcraft.jorbis.DspState;
import com.jcraft.jorbis.Info;
import java.io.IOException;
import java.io.InputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import marauroa.marauroad;


/**
 * taken from JOrbis
 **/
public class OggPlayer
 implements Runnable
{
 
 private Thread player=null;
 private InputStream bitStream=null;
 
 private static final int BUFSIZE=4096*2;
 private static int convsize=BUFSIZE*2;
 private static byte[] convbuffer=new byte[convsize];
 
 private int RETRY=3;
 private int retry=RETRY;
 
 private String playlistfile="playlist";
 private boolean icestats=false;
 
 private SyncState oy;
 private StreamState os;
 private Page og;
 private Packet op;
 private Info vi;
 private Comment vc;
 private DspState vd;
 private Block vb;
 
 private byte[] buffer=null;
 private int bytes=0;
 
 private int format;
 private int rate=0;
 private int channels=0;
 private int left_vol_scale=100;
 private int right_vol_scale=100;
 private SourceDataLine outputLine=null;
 private String current_source=null;
 private int frameSizeInBytes;
 private int bufferLengthInBytes;
 
 private String song_name;
 
 
 public OggPlayer()
 {
	song_name = "Intro_Gladiators.ogg";
 }
 
 public void play(String name)
 {	 
	if(name!=null)
	{
	 if(!name.equals(song_name))
	 {
		marauroad.trace("OggPlayer::play","D","Playing "+name);
		if(player!=null)
		{
		 player = null;
		 try
		 {
			Thread.sleep(100);
		 }
		 catch (InterruptedException e)
		 {
		 }
		}
		song_name = name;
		player = new Thread(this,"Ogg player");
		player.start();
	 }
	}
 }
 
 public void run()
 {
	Thread me = Thread.currentThread();
	while(true)
	{
	 try
	 {
		bitStream=Resources.getSoundUrl(song_name).openStream();
		play_stream(me);
		if(player!=me)
		{
		 break;
		}
		bitStream=null;
//				player=null;
	 }
	 catch (IOException e) {}
	}
 }
 
 
 private void play_stream(Thread me)
 {
	init_jorbis();
	
	retry=RETRY;
	
	loop:
	while(true)
	{
	 int eos=0;
	 
	 int index=oy.buffer(BUFSIZE);
	 buffer=oy.data;
	 try{ bytes=bitStream.read(buffer, index, BUFSIZE); }
	 catch(Exception e)
	 {
		System.err.println(e);
		return;
	 }
	 oy.wrote(bytes);
	 
	 if(oy.pageout(og)!=1)
	 {
		if(bytes<BUFSIZE)break;
		System.err.println("Input does not appear to be an Ogg bitstream.");
		return;
	 }
	 
	 os.init(og.serialno());
	 os.reset();
	 
	 vi.init();
	 vc.init();
	 
	 if(os.pagein(og)<0)
	 {
		// error; stream version mismatch perhaps
		System.err.println("Error reading first page of Ogg bitstream data.");
		return;
	 }
	 
	 retry=RETRY;
	 
	 if(os.packetout(op)!=1)
	 {
		// no page? must not be vorbis
		System.err.println("Error reading initial header packet.");
		break;
		//      return;
	 }
	 
	 if(vi.synthesis_headerin(vc, op)<0)
	 {
		// error case; not a vorbis header
		System.err.println("This Ogg bitstream does not contain Vorbis audio data.");
		return;
	 }
	 
	 int i=0;
	 
	 while(i<2)
	 {
		while(i<2)
		{
		 int result=oy.pageout(og);
		 if(result==0) break; // Need more data
		 if(result==1)
		 {
			os.pagein(og);
			while(i<2)
			{
			 result=os.packetout(op);
			 if(result==0)break;
			 if(result==-1)
			 {
				System.err.println("Corrupt secondary header.  Exiting.");
				//return;
				break loop;
			 }
			 vi.synthesis_headerin(vc, op);
			 i++;
			}
		 }
		}
		
		index=oy.buffer(BUFSIZE);
		buffer=oy.data;
		try{ bytes=bitStream.read(buffer, index, BUFSIZE); }
		catch(Exception e)
		{
		 System.err.println(e);
		 return;
		}
		if(bytes==0 && i<2)
		{
		 System.err.println("End of file before finding all Vorbis headers!");
		 return;
		}
		oy.wrote(bytes);
	 }
	 
//			{
//				byte[][] ptr=vc.user_comments;
//				StringBuffer sb=null;
//				if(acontext!=null) sb=new StringBuffer();
//
//				for(int j=0; j<ptr.length;j++)
//				{
//					if(ptr[j]==null) break;
//					System.err.println("Comment: "+new String(ptr[j], 0, ptr[j].length-1));
//					if(sb!=null)sb.append(" "+new String(ptr[j], 0, ptr[j].length-1));
//				}
//				System.err.println("Bitstream is "+vi.channels+" channel, "+vi.rate+"Hz");
//				System.err.println("Encoded by: "+new String(vc.vendor, 0, vc.vendor.length-1)+"\n");
//				if(sb!=null)acontext.showStatus(sb.toString());
//			}
	 
	 convsize=BUFSIZE/vi.channels;
	 
	 vd.synthesis_init(vi);
	 vb.init(vd);
	 
	 double[][][] _pcm=new double[1][][];
	 float[][][] _pcmf=new float[1][][];
	 int[] _index=new int[vi.channels];
	 
	 getOutputLine(vi.channels, vi.rate);
	 
	 while(eos==0)
	 {
		while(eos==0)
		{
		 
		 if(player!=me)
		 {
			//System.err.println("bye.");
			try
			{
			 //outputLine.drain();
			 //outputLine.stop();
			 //outputLine.close();
			 bitStream.close();
			}
			catch(Exception ee){}
			return;
		 }
		 
		 int result=oy.pageout(og);
		 if(result==0)break; // need more data
		 if(result==-1){ // missing or corrupt data at this page position
//	    System.err.println("Corrupt or missing data in bitstream; continuing...");
		 }
		 else
		 {
			os.pagein(og);
			while(true)
			{
			 result=os.packetout(op);
			 if(result==0)break; // need more data
			 if(result==-1){ // missing or corrupt data at this page position
				// no reason to complain; already complained above
			 }
			 else
			 {
				// we have a packet.  Decode it
				int samples;
				if(vb.synthesis(op)==0){ // test for success!
				 vd.synthesis_blockin(vb);
				}
				while((samples=vd.synthesis_pcmout(_pcmf, _index))>0)
				{
				 double[][] pcm=_pcm[0];
				 float[][] pcmf=_pcmf[0];
				 boolean clipflag=false;
				 int bout=(samples<convsize?samples:convsize);
				 
				 // convert doubles to 16 bit signed ints (host order) and
				 // interleave
				 for(i=0;i<vi.channels;i++)
				 {
					int ptr=i*2;
					//int ptr=i;
					int mono=_index[i];
					for(int j=0;j<bout;j++)
					{
					 int val=(int)(pcmf[i][mono+j]*32767.);
					 if(val>32767)
					 {
						val=32767;
						clipflag=true;
					 }
					 if(val<-32768)
					 {
						val=-32768;
						clipflag=true;
					 }
					 if(val<0) val=val|0x8000;
					 convbuffer[ptr]=(byte)(val);
					 convbuffer[ptr+1]=(byte)(val>>>8);
					 ptr+=2*(vi.channels);
					}
				 }
				 outputLine.write(convbuffer, 0, 2*vi.channels*bout);
				 vd.synthesis_read(bout);
				}
			 }
			}
			if(og.eos()!=0)eos=1;
		 }
		}
		
		if(eos==0)
		{
		 index=oy.buffer(BUFSIZE);
		 buffer=oy.data;
		 try{ bytes=bitStream.read(buffer,index,BUFSIZE); }
		 catch(Exception e)
		 {
			System.err.println(e);
			return;
		 }
		 if(bytes==-1)
		 {
			break;
		 }
		 oy.wrote(bytes);
		 if(bytes==0)eos=1;
		}
	 }
	 
	 //if(bytes==-1){
	 //  retry--;
	 //  if(retry!=0 && current_source!=null){
	 //    System.out.println("Connection is dropped. Retry("+retry+")");
	 //    init_jorbis();
	 //    try { if(bitStream!=null)bitStream.close(); }
	 //    catch(Exception e) { System.out.println(e); }
	 //    try{Thread.sleep(1000);}
	 //    catch(Exception e) { }
	 //    try{
	 //      URL url=new URL(current_source);
	 //      URLConnection urlc=url.openConnection();
	 //      bitStream=urlc.getInputStream();
	 //      continue;
	 //    }
	 //    catch(Exception e){
	 //      retry=0;
	 //    }
	 //  }
	 //}
	 
	 os.clear();
	 vb.clear();
	 vd.clear();
	 vi.clear();
	}
	
	oy.clear();
	
	//System.err.println("Done.");
	
	try
	{
	 if(bitStream!=null)bitStream.close();
	}
	catch(Exception e) { }
 }
 
 
 private SourceDataLine getOutputLine(int channels, int rate)
 {
	if(outputLine!=null || this.rate!=rate || this.channels!=channels)
	{
	 if(outputLine!=null)
	 {
		outputLine.drain();
		outputLine.stop();
		outputLine.close();
	 }
	 init_audio(channels, rate);
	 outputLine.start();
	}
	return outputLine;
 }
 
 private void init_audio(int channels, int rate)
 {
	try
	{
	 AudioFormat audioFormat =
		new AudioFormat((float)rate,
										16,
										channels,
										true,  // PCM_Signed
										false  // littleEndian
									 );
	 DataLine.Info info =
		new DataLine.Info(SourceDataLine.class,
											audioFormat,
											AudioSystem.NOT_SPECIFIED);
	 if (!AudioSystem.isLineSupported(info))
	 {
		//System.out.println("Line " + info + " not supported.");
		return;
	 }
	 
	 try
	 {
		outputLine = (SourceDataLine) AudioSystem.getLine(info);
		//outputLine.addLineListener(this);
		outputLine.open(audioFormat);
	 }
	 catch (LineUnavailableException ex)
	 {
		System.out.println("Unable to open the sourceDataLine: " + ex);
		return;
	 }
	 catch (IllegalArgumentException ex)
	 {
		System.out.println("Illegal Argument: " + ex);
		return;
	 }
	 
	 frameSizeInBytes = audioFormat.getFrameSize();
	 int bufferLengthInFrames = outputLine.getBufferSize()/frameSizeInBytes/2;
	 bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
	 
	 //buffer = new byte[bufferLengthInBytes];
	 //if(originalClassLoader!=null)
	 //  Thread.currentThread().setContextClassLoader(originalClassLoader);
	 
	 this.rate=rate;
	 this.channels=channels;
	}
	catch(Exception ee)
	{
	 System.out.println(ee);
	}
 }
 
 private   void init_jorbis()
 {
	oy=new SyncState();
	os=new StreamState();
	og=new Page();
	op=new Packet();
	
	vi=new Info();
	vc=new Comment();
	vd=new DspState();
	vb=new Block(vd);
	
	buffer=null;
	bytes=0;
	
	oy.init();
 }
 
 /**
	*
	*/
 public static void main(String[] args)
 {
	OggPlayer player = new OggPlayer();
	player.play("Fighting_Gladiators.ogg");
 }
}
