package com.rocha.MyArubaitoDash.service;

import com.rocha.MyArubaitoDash.dto.ShiftDTO;
import com.rocha.MyArubaitoDash.model.Job;
import com.rocha.MyArubaitoDash.model.Shift;
import com.rocha.MyArubaitoDash.model.Worker;
import com.rocha.MyArubaitoDash.repository.JobRepository;
import com.rocha.MyArubaitoDash.repository.ShiftRepository;
import com.rocha.MyArubaitoDash.repository.WorkerRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ShiftService {

    private final ShiftRepository shiftRepository;
    private final WorkerRepository workerRepository;
    private final JobRepository jobRepository;
    private final JobService jobService;
    private final EncryptionService encryptionService;

    @Autowired
    public ShiftService(ShiftRepository shiftRepository, WorkerRepository workerRepository, JobRepository jobRepository, JobService jobService, EncryptionService encryptionService) {
        this.shiftRepository = shiftRepository;
        this.workerRepository = workerRepository;
        this.jobRepository = jobRepository;
        this.jobService = jobService;
        this.encryptionService = encryptionService;
    }


    public ArrayList<Shift> getShiftsByJobId(int jobId) {
        return shiftRepository.findAllByJobId(jobId);
    }

    public ArrayList<Shift> getShiftsByWorkerId(int workerId) {
        return  shiftRepository.findAllByWorkerId(workerId);
    }

    public Shift getShiftById(int id) {
        Optional<Shift> shift = shiftRepository.findById(id);

        return shift.orElse(null);
    }

    public void addShift(Shift shift) {
        try {
            shiftRepository.save(shift);
        } catch (Exception e) {
            System.out.println("Unexpected Error");
            e.printStackTrace();
        }
    }

    public void updateShift(int id, ShiftDTO shiftDTO) {

        System.out.println("UPDATING SHIFT:");
        System.out.println(shiftDTO);
        try {
            Optional<Shift> optionalShift = shiftRepository.findById(id);

            if (optionalShift.isPresent()) {
                Shift shiftToBeUpdated = optionalShift.get();
                shiftToBeUpdated.setStartTime(shiftDTO.getStartTime());
                shiftToBeUpdated.setEndTime(shiftDTO.getEndTime());
                shiftToBeUpdated.setStartDate(shiftDTO.getStartDate());
                shiftToBeUpdated.setEndDate(shiftDTO.getEndDate());
                shiftToBeUpdated.setShiftType(shiftDTO.getShiftType());
                shiftToBeUpdated.setIsHoliday(shiftDTO.getIsHoliday());

                Job job = jobService.getJobById(shiftDTO.getJobId());

                if (job == null) {
                    throw new EntityNotFoundException("Job not found."); // NOT WORKING CHECK LATER
                }

                shiftToBeUpdated.setJob(job);

                float shiftDuration = calculateShiftDuration(shiftToBeUpdated);
                BigDecimal bonusRate = shiftToBeUpdated.getIsHoliday() ? BigDecimal.valueOf(1.5) : BigDecimal.ONE;
                shiftToBeUpdated.setMoneyValue(new BigDecimal(shiftDuration).multiply(job.getHourlyRate().multiply(bonusRate)));
                shiftToBeUpdated.setEncryptedMoneyValue(encryptionService.encrypt(shiftToBeUpdated.getMoneyValue().toString()));

                System.out.println(shiftToBeUpdated.getMoneyValue().toString());
                System.out.println("WILL SAVE NOW ============================");
                shiftRepository.save(shiftToBeUpdated);
            }
        } catch (Exception e) {
            System.out.println("Unexpected Error");
            e.printStackTrace();
        }
    }

    public void deleteShift(int id) {
        try {
            Optional<Shift> shiftToBeDeletedFound = shiftRepository.findById(id);
            if (shiftToBeDeletedFound.isPresent()) {
                Shift shiftToBeDeleted = shiftToBeDeletedFound.get();
                shiftRepository.delete(shiftToBeDeleted);

                System.out.println("Shift of id: " + shiftToBeDeleted.getId() + " deleted!");
            }
        } catch (Exception e) {
            System.out.println("Error deleting shift.");
            e.printStackTrace();
        }
    }

    public void createShift(ShiftDTO shiftDTO) throws EntityNotFoundException{
       try {
           Worker worker = workerRepository.findById(shiftDTO.getWorkerId()).orElse(null);
           Job job = jobService.getJobById(shiftDTO.getJobId());

           if (worker == null || job == null) {
               throw new EntityNotFoundException("Worker or job not found."); // NOT WORKING CHECK LATER
           }
           Shift shiftToAdd =  convertDTOToEntity(shiftDTO);
           shiftToAdd.setWorker(worker);
           // Calculating money value for the shift
           float shiftDuration = calculateShiftDuration(shiftToAdd);
           BigDecimal bonusRate = shiftToAdd.getIsHoliday() ? BigDecimal.valueOf(1.5) : BigDecimal.ONE;
           shiftToAdd.setMoneyValue(new BigDecimal(shiftDuration).multiply(job.getHourlyRate().multiply(bonusRate)));
           shiftToAdd.setEncryptedMoneyValue(encryptionService.encrypt(shiftToAdd.getMoneyValue().toString()));
           shiftToAdd.setJob(job);
           if (shiftToAdd.getIsHoliday() == null) {
               shiftToAdd.setIsHoliday(false);
           }
           shiftRepository.save(shiftToAdd);
       } catch (EntityNotFoundException e) {
           throw  e;
       }
       catch (Exception e) {
           System.out.println(e.getMessage());
       }
    }

    private float calculateShiftDuration(Shift shift) {
        if (shift == null) {
            return 0;
        }
        long minutesDifference = ChronoUnit.MINUTES.between(shift.getStartTime(), shift.getEndTime());
        float shiftDuration = minutesDifference / 60.0f;
        return shiftDuration >= 5 ? shiftDuration - 0.5f : shiftDuration; // - 30min break
    }

    private Shift convertDTOToEntity(ShiftDTO shiftDTO) {
        // Retrieve Worker and Job from the passed Shift object
//        Worker worker = workerRepository.findById(shiftDTO.getWorkerId()).orElse(null);
//        Job job = jobRepository.findById(shiftDTO.getJobId()).orElse(null);
        Shift shift = new Shift();
//        shift.setWorker(worker);
//        shift.setJob(job);
        shift.setStartDate(shiftDTO.getStartDate());
        shift.setStartTime(shiftDTO.getStartTime());
        shift.setEndDate(shiftDTO.getEndDate());
        shift.setEndTime(shiftDTO.getEndTime());
        shift.setShiftType(shiftDTO.getShiftType());
        shift.setIsHoliday(shiftDTO.getIsHoliday());

        return shift;
    }

    public List<Shift> getShiftsFrom(LocalDate date, int workerId, int jobId) {
        List<Shift> shifts = shiftRepository.findShiftsFromSpecificDate(jobId, workerId, date);
        return decryptShifts(shifts);
    }

    public List<Shift> getAllShiftsByWorkerFrom(LocalDate date, int workerId) {
        List<Shift> shifts = shiftRepository.findAllShiftsByWorkerFromSpecificDate(workerId, date);
        return decryptShifts(shifts);
    }

    public List<Shift> getAllShiftsInRangeByWorker(int workerId, LocalDate startDate, LocalDate endDate) {
        List<Shift> shifts = shiftRepository.findShiftsInRange(workerId, startDate, endDate);
        return decryptShifts(shifts);
    }

    public Page<Shift> getAllShiftsByWorkerFromPaginated(LocalDate date, int workerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Shift> shiftPage = shiftRepository.findAllShiftsByWorkerFromSpecificDatePaginated(workerId, date, pageable);

        List<Shift> decryptedShifts = decryptShifts(shiftPage.getContent());
        return new PageImpl<>(decryptedShifts, pageable, shiftPage.getTotalElements());
    }

    public Shift getNextShiftForWorker(int workerId) {
        LocalDateTime now = LocalDateTime.now();
        return shiftRepository.findNextShiftForWorker(workerId, now);
    }

    private List<Shift> decryptShifts(List<Shift> shifts) {
        for (Shift shift : shifts) {
            if (shift.getEncryptedMoneyValue() != null) {
                String decryptedValue = encryptionService.decrypt(shift.getEncryptedMoneyValue());
                shift.setMoneyValue(new BigDecimal(decryptedValue));
            }
        }
        return shifts;
    }

}
