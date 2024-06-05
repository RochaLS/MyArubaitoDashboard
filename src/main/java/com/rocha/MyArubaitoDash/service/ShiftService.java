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
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ShiftService {

    private final ShiftRepository shiftRepository;
    private final WorkerRepository workerRepository;
    private final JobRepository jobRepository;

    @Autowired
    public ShiftService(ShiftRepository shiftRepository, WorkerRepository workerRepository, JobRepository jobRepository) {
        this.shiftRepository = shiftRepository;
        this.workerRepository = workerRepository;
        this.jobRepository = jobRepository;
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

        System.out.println(shiftDTO);
        try {
            Optional<Shift> optionalShift = shiftRepository.findById(id);
            Worker worker = workerRepository.findById(shiftDTO.getWorkerId()).orElse(null);
            Job job = jobRepository.findById(shiftDTO.getJobId()).orElse(null);
            Shift updatedShift = convertDTOToEntity(shiftDTO);
            updatedShift.setWorker(worker);
            updatedShift.setJob(job);

            if (optionalShift.isPresent()) {
                updatedShift.setId(optionalShift.get().getId());
                shiftRepository.save(updatedShift);
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
           Job job = jobRepository.findById(shiftDTO.getJobId()).orElse(null);

           if (worker == null || job == null) {
               throw new EntityNotFoundException("Worker or job not found."); // NOT WORKING CHECK LATER
           }
           Shift shiftToAdd =  convertDTOToEntity(shiftDTO);
           shiftToAdd.setWorker(worker);
           shiftToAdd.setJob(job);
           shiftRepository.save(shiftToAdd);
       } catch (EntityNotFoundException e) {
           throw  e;
       }
       catch (Exception e) {
           System.out.println(e.getMessage());
       }
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

        return shift;
    }

    public List<Shift> getShiftsFrom(LocalDate date, int workerId, int jobId) {
        return shiftRepository.findShiftsFromSpecificDate(jobId, workerId, date);
    }

    public List<Shift> getAllShiftsByWorkerFrom(LocalDate date, int workerId) {
        return shiftRepository.findAllShiftsByWorkerFromSpecificDate(workerId, date);
    }

}
