package us.ihmc.scs2.sessionVisualizer.jfx.charts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.geometry.Side;
import javafx.scene.chart.NumberAxis;

/**
 * Tests for feature 2202 (per-variable colored Y-axes for normalized charts).
 *
 * <p>Two families of tests live here:
 * <ul>
 *    <li>Pure logic ({@link DynamicLineChart#computeRealYBounds(NumberSeries)} and
 *        {@link DynamicLineChart#getDefaultColorStyle(int)}) — no JavaFX toolkit needed, always run.</li>
 *    <li>{@link PerVariableYAxis} wrapper behavior — needs a live {@link NumberAxis}, hence a JavaFX
 *        toolkit. The toolkit is booted headlessly (Monocle) in {@link #initToolkit()}; if it cannot be
 *        hosted in this environment those tests are skipped (not failed) via {@code assumeTrue}.</li>
 * </ul>
 */
public class PerVariableYAxisTest
{
   /** Tolerance for double axis-bound comparisons; the values are exact so this only guards FP formatting. */
   private static final double EPSILON = 1.0e-12;

   private static volatile boolean fxReady = false;

   @BeforeAll
   static void initToolkit()
   {
      try
      {
         // These must be set before the toolkit initializes. Harmless if it is already up.
         System.setProperty("glass.platform", "Monocle");
         System.setProperty("monocle.platform", "Headless");
         System.setProperty("prism.order", "sw");
         System.setProperty("java.awt.headless", "true");

         CountDownLatch latch = new CountDownLatch(1);
         Platform.startup(latch::countDown);
         if (!latch.await(15, TimeUnit.SECONDS))
            throw new IllegalStateException("Timed out waiting for the JavaFX toolkit to start.");
         fxReady = true;
      }
      catch (IllegalStateException alreadyStarted)
      {
         // Platform.startup throws if the toolkit is already running (e.g. another test started it).
         fxReady = true;
      }
      catch (Throwable t)
      {
         fxReady = false;
         System.err.println("[PerVariableYAxisTest] JavaFX toolkit unavailable; skipping node-level tests: " + t);
      }
   }

   // ------------------------------------------------------------------------------------------------
   // DynamicLineChart.computeRealYBounds(NumberSeries) — the unit-test seam shared by the RAW union
   // loop and the per-variable path. Pure; no JavaFX toolkit required.
   // ------------------------------------------------------------------------------------------------

   @Test
   public void testComputeRealYBounds_plainDataBounds()
   {
      NumberSeries series = new NumberSeries("plain");
      series.yBoundsProperty().setValue(new ChartDoubleBounds(-3.0, 5.0));

      ChartDoubleBounds result = DynamicLineChart.computeRealYBounds(series);

      assertNotNull(result);
      assertEquals(-3.0, result.getLower(), EPSILON);
      assertEquals(5.0, result.getUpper(), EPSILON);
   }

   @Test
   public void testComputeRealYBounds_customOverrideWinsOverData()
   {
      NumberSeries series = new NumberSeries("custom");
      series.yBoundsProperty().setValue(new ChartDoubleBounds(-3.0, 5.0));
      // A deliberately different custom range: if the data bounds leaked through, these literals catch it.
      series.setCustomYBounds(new ChartDoubleBounds(10.0, 20.0));

      ChartDoubleBounds result = DynamicLineChart.computeRealYBounds(series);

      assertNotNull(result);
      assertEquals(10.0, result.getLower(), EPSILON);
      assertEquals(20.0, result.getUpper(), EPSILON);
   }

   @Test
   public void testComputeRealYBounds_negatedFlipsAndSwaps()
   {
      NumberSeries series = new NumberSeries("negated");
      series.yBoundsProperty().setValue(new ChartDoubleBounds(-3.0, 5.0));
      series.setNegated(true);

      ChartDoubleBounds result = DynamicLineChart.computeRealYBounds(series);

      // negate() maps [lower, upper] -> [-upper, -lower]. Expected re-derived independently, not via negate().
      assertNotNull(result);
      assertEquals(-5.0, result.getLower(), EPSILON);
      assertEquals(3.0, result.getUpper(), EPSILON);
   }

   @Test
   public void testComputeRealYBounds_customThenNegatedComposeInOrder()
   {
      NumberSeries series = new NumberSeries("customNegated");
      series.yBoundsProperty().setValue(new ChartDoubleBounds(-3.0, 5.0));
      series.setCustomYBounds(new ChartDoubleBounds(2.0, 6.0));
      series.setNegated(true);

      ChartDoubleBounds result = DynamicLineChart.computeRealYBounds(series);

      // Custom [2, 6] must win over data, THEN be negated -> [-6, -2]. Guards the override-before-negate order.
      assertNotNull(result);
      assertEquals(-6.0, result.getLower(), EPSILON);
      assertEquals(-2.0, result.getUpper(), EPSILON);
   }

   @Test
   public void testComputeRealYBounds_nullDataBoundsGivesNullEvenWithCustom()
   {
      NumberSeries series = new NumberSeries("nullData");
      // Data bounds unset (null) but a custom override IS present: the null-data guard must run FIRST.
      series.setCustomYBounds(new ChartDoubleBounds(1.0, 2.0));
      series.yBoundsProperty().setValue(null);

      assertNull(DynamicLineChart.computeRealYBounds(series),
                 "null data bounds must short-circuit to null before the custom override is consulted");
   }

   @Test
   public void testComputeRealYBounds_nullDataBoundsGivesNullEvenWhenNegated()
   {
      NumberSeries series = new NumberSeries("nullDataNegated");
      series.setNegated(true);
      series.yBoundsProperty().setValue(null);

      assertNull(DynamicLineChart.computeRealYBounds(series));
   }

   // ------------------------------------------------------------------------------------------------
   // DynamicLineChart.getDefaultColorStyle(int) — palette index wraps at 8. Expected strings are
   // hard-coded literals (NOT re-derived with the same "% 8" formula) so the test is independent.
   // ------------------------------------------------------------------------------------------------

   @Test
   public void testGetDefaultColorStyle_wrapsAtEight()
   {
      assertEquals("default-color0", DynamicLineChart.getDefaultColorStyle(0));
      assertEquals("default-color1", DynamicLineChart.getDefaultColorStyle(1));
      assertEquals("default-color7", DynamicLineChart.getDefaultColorStyle(7));
      assertEquals("default-color0", DynamicLineChart.getDefaultColorStyle(8));
      assertEquals("default-color1", DynamicLineChart.getDefaultColorStyle(9));
      assertEquals("default-color7", DynamicLineChart.getDefaultColorStyle(15));
      assertEquals("default-color0", DynamicLineChart.getDefaultColorStyle(16));
      assertEquals("default-color4", DynamicLineChart.getDefaultColorStyle(100));
   }

   // ------------------------------------------------------------------------------------------------
   // PerVariableYAxis — wrapper over a live NumberAxis. Needs the JavaFX toolkit.
   // ------------------------------------------------------------------------------------------------

   @Test
   public void testPerVariableYAxis_constructorConfiguresNode() throws Exception
   {
      assumeTrue(fxReady, "JavaFX toolkit not available in this environment");

      runOnFxAndWait(() ->
      {
         PerVariableYAxis axis = new PerVariableYAxis();
         NumberAxis node = axis.getNode();

         assertNotNull(node);
         assertEquals(Side.LEFT, node.getSide());
         assertFalse(node.isAutoRanging(), "per-variable axes set explicit real bounds, not autoranged");
         assertFalse(node.isMinorTickVisible());
         assertFalse(node.isManaged(), "the axis is positioned manually by the chart, so it must be unmanaged");
         assertTrue(node.getStyleClass().contains("axis"));
         assertTrue(node.getStyleClass().contains("per-variable-axis"));
         assertFalse(axis.hasRealBounds(), "a fresh axis has no real bounds until setRealBounds is called");
      });
   }

   @Test
   public void testPerVariableYAxis_setRealBoundsAppliesToNode() throws Exception
   {
      assumeTrue(fxReady, "JavaFX toolkit not available in this environment");

      runOnFxAndWait(() ->
      {
         PerVariableYAxis axis = new PerVariableYAxis();
         axis.setRealBounds(new ChartDoubleBounds(-2.5, 7.0));

         assertTrue(axis.hasRealBounds());
         NumberAxis node = axis.getNode();
         assertEquals(-2.5, node.getLowerBound(), EPSILON);
         assertEquals(7.0, node.getUpperBound(), EPSILON);
         // tickUnit = (upper - lower) / 5, re-derived here rather than read from the class constant.
         assertEquals((7.0 - (-2.5)) / 5.0, node.getTickUnit(), EPSILON);
      });
   }

   @Test
   public void testPerVariableYAxis_setRealBoundsNullClearsFlag() throws Exception
   {
      assumeTrue(fxReady, "JavaFX toolkit not available in this environment");

      runOnFxAndWait(() ->
      {
         PerVariableYAxis axis = new PerVariableYAxis();
         axis.setRealBounds(new ChartDoubleBounds(1.0, 4.0));
         assertTrue(axis.hasRealBounds());

         axis.setRealBounds(null);
         assertFalse(axis.hasRealBounds(), "setRealBounds(null) must mark the axis as having no real bounds");
      });
   }

   @Test
   public void testPerVariableYAxis_zeroRangeUsesFallbackTickUnit() throws Exception
   {
      assumeTrue(fxReady, "JavaFX toolkit not available in this environment");

      runOnFxAndWait(() ->
      {
         PerVariableYAxis axis = new PerVariableYAxis();
         // Degenerate range (upper == lower): tick unit must fall back to 1.0, not 0.0 (0 tick unit hangs layout).
         axis.setRealBounds(new ChartDoubleBounds(3.0, 3.0));

         assertTrue(axis.hasRealBounds());
         assertEquals(1.0, axis.getNode().getTickUnit(), EPSILON);
      });
   }

   @Test
   public void testPerVariableYAxis_applyColorStylePutsExpectedClassOnNode() throws Exception
   {
      assumeTrue(fxReady, "JavaFX toolkit not available in this environment");

      runOnFxAndWait(() ->
      {
         PerVariableYAxis axis = new PerVariableYAxis();
         axis.applyColorStyle(3);

         NumberAxis node = axis.getNode();
         assertTrue(node.getStyleClass().contains("default-color3"), "index 3 -> default-color3");
         assertTrue(node.getStyleClass().contains("axis"));
         assertTrue(node.getStyleClass().contains("per-variable-axis"));
      });
   }

   @Test
   public void testPerVariableYAxis_applyColorStyleWrapsAtEight() throws Exception
   {
      assumeTrue(fxReady, "JavaFX toolkit not available in this environment");

      runOnFxAndWait(() ->
      {
         PerVariableYAxis axis = new PerVariableYAxis();
         axis.applyColorStyle(11); // 11 % 8 == 3

         assertTrue(axis.getNode().getStyleClass().contains("default-color3"));
      });
   }

   @Test
   public void testPerVariableYAxis_applyColorStyleReplacesPreviousColor() throws Exception
   {
      assumeTrue(fxReady, "JavaFX toolkit not available in this environment");

      runOnFxAndWait(() ->
      {
         PerVariableYAxis axis = new PerVariableYAxis();
         axis.applyColorStyle(1);
         axis.applyColorStyle(2);

         NumberAxis node = axis.getNode();
         // Re-coloring (as happens on series removal / re-index) must not leave a stale color class behind.
         assertTrue(node.getStyleClass().contains("default-color2"));
         assertFalse(node.getStyleClass().contains("default-color1"),
                     "the previous color class must be cleared when the axis is recolored");
         assertTrue(node.getStyleClass().contains("axis"));
         assertTrue(node.getStyleClass().contains("per-variable-axis"));
      });
   }

   @Test
   public void testPerVariableYAxis_getNodeIsStable() throws Exception
   {
      assumeTrue(fxReady, "JavaFX toolkit not available in this environment");

      runOnFxAndWait(() ->
      {
         PerVariableYAxis axis = new PerVariableYAxis();
         assertSame(axis.getNode(), axis.getNode(), "getNode must return the same NumberAxis instance each call");
      });
   }

   // ------------------------------------------------------------------------------------------------

   /**
    * Runs {@code action} on the JavaFX application thread and blocks until it completes, re-throwing any
    * {@link AssertionError} or exception so the test fails on the calling thread.
    */
   private static void runOnFxAndWait(Runnable action) throws Exception
   {
      if (Platform.isFxApplicationThread())
      {
         action.run();
         return;
      }

      CountDownLatch done = new CountDownLatch(1);
      AtomicReference<Throwable> thrown = new AtomicReference<>();
      Platform.runLater(() ->
      {
         try
         {
            action.run();
         }
         catch (Throwable t)
         {
            thrown.set(t);
         }
         finally
         {
            done.countDown();
         }
      });

      if (!done.await(15, TimeUnit.SECONDS))
         throw new AssertionError("Timed out waiting for the JavaFX action to complete.");

      Throwable t = thrown.get();
      if (t instanceof AssertionError)
         throw (AssertionError) t;
      if (t != null)
         throw new RuntimeException(t);
   }
}
