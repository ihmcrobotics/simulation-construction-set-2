package us.ihmc.scs2.sessionVisualizer.jfx.session;

import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import us.ihmc.scs2.sessionVisualizer.jfx.session.log.VideoDataReader;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Tests in this class are disabled because the files have duplicate timestamps that will cause issues when trying to retrieve a specific timestamp
 */

public class TimestampScrubberTest
{
    private VideoDataReader.TimestampScrubber scrubber;

    private long[] actualRobotTimestamps;
    private long[] actualVideoTimestamps;

    List<Integer> alteredRobotTimestampIndexes = new ArrayList<>();

    @BeforeEach
    public void loadFileTimestamps() throws URISyntaxException, IOException
    {
        File timestampFile = new File(Objects.requireNonNull(getClass().getClassLoader().getResource("sessionLogs/GStreamer_HDMI_timestamps_100.dat")).toURI());

        scrubber = new VideoDataReader.TimestampScrubber(timestampFile, true, false);

        // Need to have one video in the log or this will fail
        actualRobotTimestamps = scrubber.getRobotTimestampsFromFile();
        actualVideoTimestamps = scrubber.getVideoTimestampsFromFile();
    }

    @Test
    public void interpolateDuplicates()
    {
        // Go through the robot timestamps in order and check the next one is larger
        for (int i = 1; i < actualRobotTimestamps.length; i++)
        {
            scrubber.replaceDuplicateTimestamps(i);
        }

        System.out.println(scrubber.alteredRobotTimestampIndexes);
        System.out.println(scrubber.alteredRobotTimestampIndexes.size());
    }

    @Test
    public void goingBackwards()
    {
        // Go through the robot timestamps in order and check the next one is larger
        for (int i = actualRobotTimestamps.length; i > 0; i--)
        {
            scrubber.replaceDuplicateTimestamps(i);
        }

        System.out.println(scrubber.alteredRobotTimestampIndexes);
        System.out.println(scrubber.alteredRobotTimestampIndexes.size());
    }

    @Test
    public void random()
    {
        // Go through the robot timestamps in order and check the next one is larger
        for (int i = actualRobotTimestamps.length; i > 0; i-=2)
        {
            scrubber.replaceDuplicateTimestamps(i);
        }

        System.out.println(scrubber.alteredRobotTimestampIndexes);
        System.out.println(scrubber.alteredRobotTimestampIndexes.size());
    }

    @Test
    public void testChronologicallyIncreasingRobotTimestamps()
    {
        long previousTimestamp;

        // Go through the robot timestamps in order and check the next one is larger
        for (int i = 1; i < actualRobotTimestamps.length; i++)
        {
            previousTimestamp = actualRobotTimestamps[i - 1];
            long currentTimestamp = actualRobotTimestamps[i];

            //TODO fix duplicate timestamps
            if (currentTimestamp == previousTimestamp)
            {
                System.out.println(currentTimestamp);
                continue;
            }

            Assertions.assertTrue(currentTimestamp > previousTimestamp, "Cureent: " + currentTimestamp + "\n Previous: " + previousTimestamp);
        }
    }

    @Test
    public void testStandardDeviationBetweenRobotTimestamps()
    {
        StandardDeviation standardDeviation = new StandardDeviation();

        double[] copyRobotTimestamps = new double[actualRobotTimestamps.length];

        for (int i = 1; i < actualRobotTimestamps.length; i++)
        {
            long currentDelta = actualRobotTimestamps[i] - actualRobotTimestamps[i - 1];
            copyRobotTimestamps[i - 1] = currentDelta;
        }

        System.out.println("robotTimestamps Length: " + actualRobotTimestamps.length);
        System.out.println("copyOfRobotTimestamps Length: " + copyRobotTimestamps.length);
        System.out.println("Standard Deviation: " + (long) standardDeviation.evaluate(copyRobotTimestamps));
    }

    @Disabled
    @Test
    public void testDeltaStatisticsBetweenRobotTimestamps()
    {
        long previousTimestamp;
        long delta = 0;
        long smallestDelta = 1000000000;
        long largestDelta = 0;
        int duplicates = 0;
        long whereItHappened = 0;

        // Go through the robot timestamps in order and check the next one is larger
        for (int i = 1; i < actualRobotTimestamps.length; i++)
        {
            previousTimestamp = actualRobotTimestamps[i - 1];
            long currentTimestamp = actualRobotTimestamps[i];

//            Assertions.assertTrue(currentTimestamp > previousTimestamp, "Cureent: " + currentTimestamp + "\n Previous: " + previousTimestamp);

            long currentDelta = currentTimestamp - previousTimestamp;
            delta += currentDelta;

            if (currentDelta < smallestDelta)
                smallestDelta = currentDelta;

            if (currentDelta > largestDelta)
            {
                largestDelta = currentDelta;
                whereItHappened = previousTimestamp;
            }

        }

        delta = delta / (actualRobotTimestamps.length - duplicates);

        System.out.println("Smallest Delta: " + smallestDelta);
        System.out.println("Largest Delta: " + largestDelta);
        System.out.println("Duplicate robotTimestamps: " + duplicates);
        System.out.println("Average delta for robotTimestamps: " + delta);
        System.out.println("Where it happened: " + whereItHappened);
    }

    @Disabled
    @Test
    public void testGoingThroughRobotTimestampsInOrder()
    {
        // Go through the robot timestamps in order and see if we get the desired video timestamp
        for (int i = 0; i < actualRobotTimestamps.length; i++)
        {
            scrubber.getVideoTimestamp(actualRobotTimestamps[i]);
            Assertions.assertEquals(scrubber.getCurrentVideoTimestamp(), actualVideoTimestamps[i]);
        }
    }

    @Disabled
    @Test
    public void testGoingThroughRobotTimestampsEveryOther()
    {
        // Go through the robot timestamps by +=2, so we skip every other frame and see if we get the desired video timestamp
        for (int i = 0; i < actualRobotTimestamps.length ; i+=2)
        {
            scrubber.getVideoTimestamp(actualRobotTimestamps[i]);
            Assertions.assertEquals(scrubber.getCurrentVideoTimestamp(), actualVideoTimestamps[i], "For look index: " + i);
        }
    }

    @Disabled
    @Test
    public void testGettingRandomTimestamp()
    {
        // Test grabbing random robot timestamps and checking to make sure we get the correct video timestamp
        // These robot timestamps need to be unique or the binary search will fail to get the correct video timestamp
        scrubber.getVideoTimestamp(actualRobotTimestamps[26]);
        Assertions.assertEquals(scrubber.getCurrentVideoTimestamp(), actualVideoTimestamps[26]);

        scrubber.getVideoTimestamp(actualRobotTimestamps[40]);
        Assertions.assertEquals(scrubber.getCurrentVideoTimestamp(), actualVideoTimestamps[40]);

        scrubber.getVideoTimestamp(actualRobotTimestamps[34]);
        Assertions.assertEquals(scrubber.getCurrentVideoTimestamp(), actualVideoTimestamps[34]);
    }

    @Disabled
    @Test
    public void testGoingThroughRobotTimestampsBackwards()
    {
        for (int i = actualRobotTimestamps.length - 1; i > 0; i--)
        {
            scrubber.getVideoTimestamp(actualRobotTimestamps[i]);
            Assertions.assertEquals(scrubber.getCurrentVideoTimestamp(), actualVideoTimestamps[i]);
        }
    }

    // Method to prevent reading duplicate timestamps, useful for testing future tests and bugs
    public boolean robotTimestampIsNotUnique(int index)
    {
        boolean checkPrevious = false;
        boolean checkNext = false;

        if (index - 1 > 0)
            checkPrevious = actualRobotTimestamps[index] == actualRobotTimestamps[index - 1];

        if (index + 1 < actualRobotTimestamps.length)
            checkNext = actualRobotTimestamps[index] == actualRobotTimestamps[index + 1];

        return checkPrevious || checkNext;
    }
}
