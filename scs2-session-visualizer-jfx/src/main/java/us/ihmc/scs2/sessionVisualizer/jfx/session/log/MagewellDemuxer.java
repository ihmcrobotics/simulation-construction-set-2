package us.ihmc.scs2.sessionVisualizer.jfx.session.log;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import us.ihmc.robotDataLogger.Camera;

import java.io.File;

public class MagewellDemuxer
{
    public String videoPath;
    public String timestampPath;

    private final FFmpegFrameGrabber grabber;

    public MagewellDemuxer(File dataDirectory, Camera camera)
    {
        videoPath = dataDirectory.getAbsolutePath() + "/" + camera.getVideoFile();
        timestampPath = dataDirectory.getAbsolutePath() + "/" + camera.getTimestampFile();

        try
        {
//            When trying to creat a new frame grabber things were not working, maybe we can pass in the one being used rather then creat a new one?
            grabber = new FFmpegFrameGrabber(videoPath);
            grabber.start();
        }
        catch (FrameGrabber.Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public String getName()
    {
        return "MageWell Demuxer";
    }

    public int getImageHeight()
    {
        return grabber.getImageHeight();
    }

    public int getImageWidth()
    {
        return grabber.getImageWidth();
    }

    public long getCurrentPTS()
    {
        return grabber.getTimestamp();
    }

    public void seekToPTS(long videoTimestamp)
    {
        try
        {
            grabber.setTimestamp(videoTimestamp);
        }
        catch (FFmpegFrameGrabber.Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public int getFrameNumber()
    {
        return grabber.getFrameNumber();
    }

    public Frame getNextFrame()
    {
        try
        {
            return grabber.grabFrame();
        }
        catch (FrameGrabber.Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public double getFrameRate()
    {
        return grabber.getVideoFrameRate();
    }
}
