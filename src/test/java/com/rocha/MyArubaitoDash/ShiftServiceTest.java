package com.rocha.MyArubaitoDash;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rocha.MyArubaitoDash.dto.ShiftDTO;
import com.rocha.MyArubaitoDash.model.Job;
import com.rocha.MyArubaitoDash.model.Shift;
import com.rocha.MyArubaitoDash.model.Worker;
import com.rocha.MyArubaitoDash.repository.JobRepository;
import com.rocha.MyArubaitoDash.repository.ShiftRepository;
import com.rocha.MyArubaitoDash.repository.WorkerRepository;
import com.rocha.MyArubaitoDash.service.ShiftService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ShiftServiceTest {
    @Mock
    private ShiftRepository shiftRepository;

    @Mock
    private WorkerRepository workerRepository;

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private ShiftService shiftService;

    private ShiftDTO shiftDTO;
    private Shift shift;
    private Worker worker;
    private Job job;

    @BeforeEach
    void setUp() {
        shiftDTO = new ShiftDTO();
        shiftDTO.setStartDate(LocalDate.now());
        shiftDTO.setStartTime(LocalTime.of(9, 0));
        shiftDTO.setEndDate(LocalDate.now());
        shiftDTO.setEndTime(LocalTime.of(17, 0));
        shiftDTO.setShiftType("Opening");
        shiftDTO.setWorkerId(1);
        shiftDTO.setJobId(1);

        shift = new Shift();
        shift.setStartDate(LocalDate.now());
        shift.setStartTime(LocalTime.of(9, 0));
        shift.setEndDate(LocalDate.now());
        shift.setEndTime(LocalTime.of(17, 0));
        shift.setShiftType("Opening");

        worker = new Worker();
        worker.setId(1);

        job = new Job();
        job.setId(1);
    }

    @Test
    public void testCreateShift_Success() {
        // When set mocks behaviour
        when(workerRepository.findById(1)).thenReturn(Optional.of(worker));
        when(jobRepository.findById(1)).thenReturn(Optional.of(job));
        when(shiftRepository.save(any(Shift.class))).thenReturn(shift);

        // Actually tests it
        shiftService.createShift(shiftDTO);

        verify(shiftRepository, times(1)).save(any(Shift.class));
    }

    @Test
    public void testCreateShift_EntityNotFoundException() {
        when(workerRepository.findById(1)).thenReturn(Optional.empty());
        when(jobRepository.findById(1)).thenReturn(Optional.empty());

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            shiftService.createShift(shiftDTO);
        });

        assertEquals("Worker or job not found.", thrown.getMessage());
    }

    @Test
    public void testGetShiftById_Success() {
        when(shiftRepository.findById(1)).thenReturn(Optional.of(shift));

        Shift foundShift = shiftService.getShiftById(1);

        assertNotNull(foundShift);
        assertEquals(shift.getStartDate(), foundShift.getStartDate());
        verify(shiftRepository, times(1)).findById(1);
    }

    @Test
    public void testGetShiftById_NotFound() {
        when(shiftRepository.findById(1)).thenReturn(Optional.empty());

        Shift foundShift = shiftService.getShiftById(1);

        assertNull(foundShift);
        verify(shiftRepository, times(1)).findById(1);
    }

    @Test
    public void testUpdateShift_Success() {
        when(shiftRepository.findById(1)).thenReturn(Optional.of(shift));
        when(jobRepository.findById(1)).thenReturn(Optional.of(job));
        when(shiftRepository.save(any(Shift.class))).thenReturn(shift);

        shiftService.updateShift(1, shiftDTO);

        verify(shiftRepository, times(1)).findById(1);
        verify(shiftRepository, times(1)).save(any(Shift.class));
    }

    @Test
    public void testDeleteShift_Success() {
        when(shiftRepository.findById(1)).thenReturn(Optional.of(shift));

        shiftService.deleteShift(1);

        verify(shiftRepository, times(1)).findById(1);
        verify(shiftRepository, times(1)).delete(shift);
    }

    @Test
    public void testGetShiftsByJobId_Success() {
        ArrayList<Shift> shifts = new ArrayList<>();
        shifts.add(shift);
        when(shiftRepository.findAllByJobId(1)).thenReturn(shifts);

        ArrayList<Shift> foundShifts = shiftService.getShiftsByJobId(1);

        assertNotNull(foundShifts);
        assertFalse(foundShifts.isEmpty());
        verify(shiftRepository, times(1)).findAllByJobId(1);
    }

//    @Test
//    public void testGetShiftsByWorkerId_Success() {
//        List<Shift> shifts = new ArrayList<>();
//        shifts.add(shift);
//        when(shiftRepository.findAllByWorkerId(1)).thenReturn(shifts);
//
//        List<Shift> foundShifts = shiftService.getShiftsByWorkerId(1);
//
//        assertNotNull(foundShifts);
//        assertFalse(foundShifts.isEmpty());
//        verify(shiftRepository, times(1)).findAllByWorkerId(1);
//    }

    @Test
    public void testShiftDTODeserializationWithProblematicCharacters() throws Exception {
        String problematicJson = """
        [{
            "worker_id": 16,
            "job_id": 1,
            "startDate": "2025-07-20",
            "startTime": "10:00:00?AM",
            "endDate": "2025-07-20",
            "endTime": "16:30:00?PM",
            "shiftType": "Not Specified",
            "isHoliday": false,
            "id": 123
        }]
        """;

        ObjectMapper mapper = new ObjectMapper();

        // This should throw the same error you're seeing
        assertThrows(JsonProcessingException.class, () -> {
            mapper.readValue(problematicJson, new TypeReference<ArrayList<ShiftDTO>>() {});
        });
    }


}
