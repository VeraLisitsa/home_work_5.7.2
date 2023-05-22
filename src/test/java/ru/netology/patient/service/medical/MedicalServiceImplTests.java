package ru.netology.patient.service.medical;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import ru.netology.patient.entity.BloodPressure;
import ru.netology.patient.entity.HealthInfo;
import ru.netology.patient.entity.PatientInfo;
import ru.netology.patient.repository.PatientInfoRepository;
import ru.netology.patient.service.alert.SendAlertService;

import java.math.BigDecimal;
import java.util.stream.Stream;

public class MedicalServiceImplTests {
    SendAlertService alertService = Mockito.mock(SendAlertService.class);
    PatientInfoRepository patientInfoRepository = Mockito.mock(PatientInfoRepository.class);
    MedicalService medService;

    @BeforeEach
    public void beforeEach() {
        medService = new MedicalServiceImpl(patientInfoRepository, alertService);
    }

    @AfterEach
    public void afterEach() {
        medService = null;
    }

    @ParameterizedTest
    @MethodSource("methodSourcePressure")
    public void testCheckBloodPressure(PatientInfo patientInfo, BloodPressure bloodPressureActual, int expectedTimes) {

        Mockito.when(patientInfoRepository.getById(Mockito.anyString()))
                .thenReturn(patientInfo);

        medService.checkBloodPressure(Mockito.anyString(), bloodPressureActual);

        Mockito.verify(alertService, Mockito.times(expectedTimes)).send(Mockito.anyString());
    }

    @ParameterizedTest
    @MethodSource("methodSourceTemperature")
    public void testCheckTemperature(PatientInfo patientInfo, BigDecimal temperatureActual, int expectedTimes) {

        Mockito.when(patientInfoRepository.getById(Mockito.anyString()))
                .thenReturn(patientInfo);

        medService.checkTemperature(Mockito.anyString(), temperatureActual);

        Mockito.verify(alertService, Mockito.times(expectedTimes)).send(Mockito.anyString());
    }

    public static Stream<Arguments> methodSourcePressure() {
        return Stream.of(
                Arguments.of(new PatientInfo("user1", null, null, null, new HealthInfo(null, new BloodPressure(110, 70))),
                        new BloodPressure(120, 70), 1),
                Arguments.of(new PatientInfo("user1", null, null, null, new HealthInfo(null, new BloodPressure(110, 70))),
                        new BloodPressure(110, 70), 0));

    }

    public static Stream<Arguments> methodSourceTemperature() {
        return Stream.of(
                Arguments.of(new PatientInfo("user1", null, null, null, new HealthInfo(new BigDecimal(36.6), null)),
                        new BigDecimal(35.0), 1),
                Arguments.of(new PatientInfo("user1", null, null, null, new HealthInfo(new BigDecimal(36.6), null)),
                        new BigDecimal(36.6), 0));

    }

    @Test
    public void testCheckBloodPressureArgumentCapture() {
        Mockito.when(patientInfoRepository.getById(Mockito.anyString()))
                .thenReturn(new PatientInfo("user1", null, null, null, new HealthInfo(null, new BloodPressure(110, 70))));

        medService.checkBloodPressure("user1", new BloodPressure(110, 90));
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(alertService).send(argumentCaptor.capture());
        Assertions.assertEquals("Warning, patient with id: user1, need help", argumentCaptor.getValue());
    }

    @Test
    public void testCheckTemperatureArgumentCapture() {
        Mockito.when(patientInfoRepository.getById(Mockito.anyString()))
                .thenReturn(new PatientInfo("user1", null, null, null, new HealthInfo(new BigDecimal(36.8), null)));

        medService.checkTemperature("user1", new BigDecimal(35.1));
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(alertService).send(argumentCaptor.capture());
        Assertions.assertEquals("Warning, patient with id: user1, need help", argumentCaptor.getValue());
    }
}
