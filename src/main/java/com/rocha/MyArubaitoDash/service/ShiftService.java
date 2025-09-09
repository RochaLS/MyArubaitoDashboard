package com.rocha.MyArubaitoDash.service;

import com.rocha.MyArubaitoDash.controller.IncomeController;
import com.rocha.MyArubaitoDash.dto.ShiftDTO;
import com.rocha.MyArubaitoDash.model.Job;
import com.rocha.MyArubaitoDash.model.Shift;
import com.rocha.MyArubaitoDash.model.Worker;
import com.rocha.MyArubaitoDash.model.WorkerSettings;
import com.rocha.MyArubaitoDash.repository.ShiftRepository;
import com.rocha.MyArubaitoDash.util.OwnershipVerifier;
import com.rocha.MyArubaitoDash.util.ShiftHelper;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ShiftService {

    private final ShiftRepository shiftRepository;
    private final JobService jobService;
    private final EncryptionService encryptionService;
    private final OwnershipVerifier ownershipVerifier;
    private final WorkerSettingsService workerSettingsService;
    private final ShiftHelper shiftHelper;

    private static final Logger logger = LoggerFactory.getLogger(ShiftService.class);

    @Autowired
    public ShiftService(
            ShiftRepository shiftRepository,
            JobService jobService,
            EncryptionService encryptionService,
            OwnershipVerifier ownershipVerifier,
            ShiftHelper shiftHelper,
            WorkerSettingsService workerSettingsService
    ) {
        this.shiftRepository = shiftRepository;
        this.jobService = jobService;
        this.encryptionService = encryptionService;
        this.ownershipVerifier = ownershipVerifier;
        this.workerSettingsService = workerSettingsService;
        this.shiftHelper = shiftHelper;
    }

    public List<ShiftDTO> getShiftsFromRange(LocalDate fromDate, LocalDate endDate, int workerId) {
        List<Shift> shifts =  shiftRepository.getAllShiftsInRangeWithJob(workerId, fromDate, endDate);
        if (shifts.isEmpty()) {
            return Collections.emptyList();
        }
        ownershipVerifier.checkShiftOwnership(shifts.get(0));
        List<Shift> decryptedShifts = decryptShifts(shifts);

        BigDecimal holidayMultiplier = workerSettingsService.getSettingsByWorkerId(workerId).getPayMultiplier();

        return shiftHelper.createShiftDTOs(decryptedShifts, workerId, holidayMultiplier, shiftHelper.getJobMapForShifts(decryptedShifts));


    }

    public ArrayList<Shift> getShiftsByJobId(int jobId) {
        Job job = jobService.getJobById(jobId);
        ownershipVerifier.checkJobOwnership(job);
        return shiftRepository.findAllByJobId(jobId);
    }

    public List<Shift> getShiftsByWorkerId(int workerId) {
        ownershipVerifier.checkWorkerIdOwnership(workerId);
        return decryptShifts(shiftRepository.findAllByWorkerId(workerId));
    }

    public Shift getShiftById(int id) {
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Shift not found"));
        ownershipVerifier.checkShiftOwnership(shift);
        return decryptShifts(List.of(shift)).get(0);
    }

    public void createShift(ShiftDTO shiftDTO) {
        Worker currentWorker = ownershipVerifier.getCurrentWorker();
        ownershipVerifier.checkWorkerIdOwnership(shiftDTO.getWorkerId());

        Job job = jobService.getJobById(shiftDTO.getJobId());
        ownershipVerifier.checkJobOwnership(job);

        Shift shift = convertDTOToEntity(shiftDTO);
        shift.setWorker(currentWorker);

        LocalTime start = shift.getStartTime();
        LocalTime end = shift.getEndTime();

        if (start.equals(job.getOpeningTime())) {
            shift.setShiftType("Opening");
        } else if (end.equals(job.getClosingTime())) {
            shift.setShiftType("Closing");
        } else {
            shift.setShiftType("Mid");
        }

        logger.info("Assigning shift type for shift: start={}, end={}, jobOpening={}, jobClosing={}",
                start, end, job.getOpeningTime(), job.getClosingTime());


        if (job.isHasSetStoreHours()) {
            if (job.getOpeningTime() != null && !start.isAfter(job.getOpeningTime())) {
                shift.setShiftType("Opening");
                logger.info("Shift type assigned: Opening (starts before or at opening)");
            } else if (job.getClosingTime() != null && !end.isBefore(job.getClosingTime())) {
                shift.setShiftType("Closing");
                logger.info("Shift type assigned: Closing (ends after or at closing)");
            } else {
                shift.setShiftType("Mid");
                logger.info("Shift type assigned: Mid (does not match opening or closing)");
            }
        } else {
            shift.setShiftType("Not Specified");
        }




        shift.setJob(job);
        calculateAndSetMoney(shift, job);

        if (shift.getIsHoliday() == null) {
            shift.setIsHoliday(false);
        }

        shiftRepository.save(shift);
    }

    public void updateShift(int id, ShiftDTO shiftDTO) {
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Shift not found"));
        ownershipVerifier.checkShiftOwnership(shift);

        Job job = jobService.getJobById(shiftDTO.getJobId());
        ownershipVerifier.checkJobOwnership(job);

        shift.setStartTime(shiftDTO.getStartTime());
        shift.setEndTime(shiftDTO.getEndTime());
        shift.setStartDate(shiftDTO.getStartDate());
        shift.setEndDate(shiftDTO.getEndDate());
        shift.setShiftType(shiftDTO.getShiftType());
        shift.setIsHoliday(shiftDTO.getIsHoliday());


        shift.setJob(job);

        LocalTime start = shift.getStartTime();
        LocalTime end = shift.getEndTime();

        logger.info("Assigning shift type for shift: start={}, end={}, jobOpening={}, jobClosing={}",
                start, end, job.getOpeningTime(), job.getClosingTime());



        if (job.isHasSetStoreHours()) {
            if (job.getOpeningTime() != null && !start.isAfter(job.getOpeningTime())) {
                shift.setShiftType("Opening");
                logger.info("Shift type assigned: Opening (starts before or at opening)");
            } else if (job.getClosingTime() != null && !end.isBefore(job.getClosingTime())) {
                shift.setShiftType("Closing");
                logger.info("Shift type assigned: Closing (ends after or at closing)");
            } else {
                shift.setShiftType("Mid");
                logger.info("Shift type assigned: Mid (does not match opening or closing)");
            }
        } else {
            shift.setShiftType("Not Specified");
        }

        calculateAndSetMoney(shift, job);
        shiftRepository.save(shift);
    }

    public void deleteShift(int id) {
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Shift not found"));
        ownershipVerifier.checkShiftOwnership(shift);
        shiftRepository.delete(shift);
    }

    public List<Shift> getShiftsFrom(LocalDate date, int workerId, int jobId) {
        ownershipVerifier.checkWorkerIdOwnership(workerId);
        Job job = jobService.getJobById(jobId);
        ownershipVerifier.checkJobOwnership(job);

        return decryptShifts(shiftRepository.findShiftsFromSpecificDate(jobId, workerId, date));
    }

    public List<Shift> getAllShiftsByWorkerFrom(LocalDate date, int workerId) {
        ownershipVerifier.checkWorkerIdOwnership(workerId);
        return decryptShifts(shiftRepository.findAllShiftsByWorkerFromSpecificDate(workerId, date));
    }

    public List<Shift> getAllShiftsInRangeByWorker(int workerId, LocalDate startDate, LocalDate endDate) {
        ownershipVerifier.checkWorkerIdOwnership(workerId);
        return decryptShifts(shiftRepository.findShiftsInRange(workerId, startDate, endDate));
    }

    public Page<Shift> getAllShiftsByWorkerFromPaginated(LocalDate date, int workerId, int page, int size) {
        ownershipVerifier.checkWorkerIdOwnership(workerId);
        Pageable pageable = PageRequest.of(page, size);
        Page<Shift> shiftPage = shiftRepository.findAllShiftsByWorkerFromSpecificDatePaginated(workerId, date, pageable);

        List<Shift> decrypted = decryptShifts(shiftPage.getContent());
        return new PageImpl<>(decrypted, pageable, shiftPage.getTotalElements());
    }

    public Shift getNextShiftForWorker(int workerId) {
        ownershipVerifier.checkWorkerIdOwnership(workerId);
        return shiftRepository.findNextShiftForWorker(workerId, LocalDateTime.now());
    }

    private void calculateAndSetMoney(Shift shift, Job job) {
        float duration = calculateShiftDuration(shift);
        BigDecimal bonus = shift.getIsHoliday() ? BigDecimal.valueOf(1.5) : BigDecimal.ONE;
        BigDecimal value = new BigDecimal(duration).multiply(job.getHourlyRate().multiply(bonus));
        shift.setMoneyValue(value);
        shift.setEncryptedMoneyValue(encryptionService.encrypt(value.toString()));
    }

    private float calculateShiftDuration(Shift shift) {
        long minutes = ChronoUnit.MINUTES.between(shift.getStartTime(), shift.getEndTime());
        float hours = minutes / 60.0f;
        return hours >= 5 ? hours - 0.5f : hours; // Apply break if >= 5 hrs
    }

    private Shift convertDTOToEntity(ShiftDTO dto) {
        Shift shift = new Shift();
        shift.setStartDate(dto.getStartDate());
        shift.setEndDate(dto.getEndDate());
        shift.setStartTime(dto.getStartTime());
        shift.setEndTime(dto.getEndTime());
        shift.setShiftType(dto.getShiftType());
        shift.setIsHoliday(dto.getIsHoliday());
        return shift;
    }

//    private ShiftDTO convertToDTO(Shift shift) {
//        BigDecimal duration = BigDecimal.valueOf(calculateShiftDuration(shift));
//        ShiftDTO dto = new ShiftDTO();
//        dto.setId(shift.getId());
//        dto.setWorkerId(shift.getWorker().getId());
//        dto.setJobId(shift.getJob() != null ? shift.getJob().getId() : null);
//        dto.setStartDate(shift.getStartDate());
//        dto.setStartTime(shift.getStartTime());
//        dto.setEndDate(shift.getEndDate());
//        dto.setEndTime(shift.getEndTime());
//        dto.setShiftType(shift.getShiftType());
//        dto.setIsHoliday(shift.getIsHoliday());
//        dto.setMoneyValue(shift.getMoneyValue());
//        return dto;
//    }

    private List<Shift> decryptShifts(List<Shift> shifts) {
        for (Shift shift : shifts) {
            if (shift.getEncryptedMoneyValue() != null) {
                String decrypted = encryptionService.decrypt(shift.getEncryptedMoneyValue());
                shift.setMoneyValue(new BigDecimal(decrypted));
            }
        }
        return shifts;
    }
}