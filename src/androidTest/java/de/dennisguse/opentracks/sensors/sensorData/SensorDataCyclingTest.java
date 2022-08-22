package de.dennisguse.opentracks.sensors.sensorData;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.dennisguse.opentracks.data.models.Cadence;
import de.dennisguse.opentracks.data.models.Distance;
import de.dennisguse.opentracks.sensors.UintUtils;

@RunWith(AndroidJUnit4.class)
public class SensorDataCyclingTest {

    @Test
    public void compute_cadence_1() {
        // given
        SensorDataCyclingCadence previous = new SensorDataCyclingCadence("sensorAddress", "sensorName", 1, 1024); // 1s
        SensorDataCyclingCadence current = new SensorDataCyclingCadence("sensorAddress", "sensorName", 2, 2048); // 2s

        // when
        current.compute(previous);

        // then
        assertEquals(60, current.getValue().getRPM(), 0.01);
    }

    @Test
    public void compute_cadence_2() {
        // given
        SensorDataCyclingCadence previous = new SensorDataCyclingCadence("sensorAddress", "sensorName", 1, 6184);
        SensorDataCyclingCadence current = new SensorDataCyclingCadence("sensorAddress", "sensorName", 2, 8016);

        // when
        current.compute(previous);

        // then
        assertEquals(33.53, current.getValue().getRPM(), 0.01);
    }

    @Test
    public void compute_cadence_sameCount() {
        // given
        SensorDataCyclingCadence previous = new SensorDataCyclingCadence("sensorAddress", "sensorName", 1, 1024);
        SensorDataCyclingCadence current = new SensorDataCyclingCadence("sensorAddress", "sensorName", 1, 2048);

        // when
        current.compute(previous);

        // then
        assertEquals(Cadence.of(0), current.getValue());
    }


    @Test
    public void compute_cadence_sameTime() {
        // given
        SensorDataCyclingCadence previous = new SensorDataCyclingCadence("sensorAddress", "sensorName", 1, 1024);
        SensorDataCyclingCadence current = new SensorDataCyclingCadence("sensorAddress", "sensorName", 2, 1024);

        // when
        current.compute(previous);

        // then
        assertFalse(current.hasValue());
    }

    @Test
    public void compute_cadence_rollOverTime() {
        // given
        SensorDataCyclingCadence previous = new SensorDataCyclingCadence("sensorAddress", "sensorName", 1, UintUtils.UINT16_MAX - 1024);
        SensorDataCyclingCadence current = new SensorDataCyclingCadence("sensorAddress", "sensorName", 2, 0);

        // when
        current.compute(previous);

        // then
        assertEquals(60, current.getValue().getRPM(), 0.01);
    }

    @Ignore("Disabled from #953")
    @Test
    @Deprecated
    public void compute_cadence_rollOverCount() {
        // given
        SensorDataCyclingCadence previous = new SensorDataCyclingCadence("sensorAddress", "sensorName", UintUtils.UINT32_MAX - 1, 1024);
        SensorDataCyclingCadence current = new SensorDataCyclingCadence("sensorAddress", "sensorName", 0, 2048);

        // when
        current.compute(previous);

        // then
        assertEquals(60, current.getValue().getRPM(), 0.01);
    }

    @Test
    public void compute_cadence_overflow() {
        // given
        SensorDataCyclingCadence previous = new SensorDataCyclingCadence("sensorAddress", "sensorName", UintUtils.UINT32_MAX - 1, 1024);
        SensorDataCyclingCadence current = new SensorDataCyclingCadence("sensorAddress", "sensorName", 0, 2048);

        // when
        current.compute(previous);

        // then
        assertNull(current.getValue());
    }

    @Test
    public void compute_speed() {
        // given
        SensorDataCyclingDistanceSpeed previous = new SensorDataCyclingDistanceSpeed("sensorAddress", "sensorName", 1, 6184);
        SensorDataCyclingDistanceSpeed current = new SensorDataCyclingDistanceSpeed("sensorAddress", "sensorName", 2, 8016);

        // when
        current.compute(previous, Distance.ofMM(2150));

        // then
        assertEquals(2.15, current.getValue().getDistance().toM(), 0.01);
        assertEquals(1.20, current.getValue().getSpeed().toMPS(), 0.01);
    }

    @Ignore("Disabled from #953")
    @Test
    @Deprecated
    public void compute_speed_rollOverCount() {
        // given
        SensorDataCyclingDistanceSpeed previous = new SensorDataCyclingDistanceSpeed("sensorAddress", "sensorName", UintUtils.UINT32_MAX - 1, 1024);
        SensorDataCyclingDistanceSpeed current = new SensorDataCyclingDistanceSpeed("sensorAddress", "sensorName", 0, 2048);

        // when
        current.compute(previous, Distance.ofMM(2000));

        // then
        assertEquals(2, current.getValue().getDistance().toM(), 0.01);
        assertEquals(2, current.getValue().getSpeed().toMPS(), 0.01);
    }

    @Test
    public void compute_speed_overflow() {
        // given
        SensorDataCyclingDistanceSpeed previous = new SensorDataCyclingDistanceSpeed("sensorAddress", "sensorName", UintUtils.UINT32_MAX - 1, 1024);
        SensorDataCyclingDistanceSpeed current = new SensorDataCyclingDistanceSpeed("sensorAddress", "sensorName", 0, 2048);

        // when
        current.compute(previous, Distance.ofMM(2000));

        // then
        assertNull(current.getValue());
    }

    @Test
    public void equals_speed_with_no_data() {
        // given
        SensorDataCyclingDistanceSpeed previous = new SensorDataCyclingDistanceSpeed("sensorAddress");
        SensorDataCyclingDistanceSpeed current = new SensorDataCyclingDistanceSpeed("sensorAddress", "sensorName", 0, 2048);

        // when
        previous.toString();

        // then
        assertNotEquals(previous, current);
        assertNotEquals(previous, previous);
    }

    @Test
    public void equals_cadence_with_no_data() {
        // given
        SensorDataCyclingCadence previous = new SensorDataCyclingCadence("sensorAddress");
        SensorDataCyclingCadence current = new SensorDataCyclingCadence("sensorAddress", "sensorName", 0, 2048);

        // when
        previous.toString();

        // then
        assertNotEquals(previous, current);
        assertNotEquals(previous, previous);
    }
}