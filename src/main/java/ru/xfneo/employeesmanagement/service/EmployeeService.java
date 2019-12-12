package ru.xfneo.employeesmanagement.service;

import feign.Response;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.xfneo.employeesmanagement.client.DepartmentClient;
import ru.xfneo.employeesmanagement.model.DepartmentsToReplaceDto;
import ru.xfneo.employeesmanagement.model.Employee;
import ru.xfneo.employeesmanagement.repository.EmployeeRepository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final DepartmentClient departmentClient;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository, DepartmentClient departmentClient) {
        this.employeeRepository = employeeRepository;
        this.departmentClient = departmentClient;
    }

    private boolean nonexistentDepartment(long departmentId) {
        Response response = departmentClient.checkDepartment(departmentId);
        return response.status() < 200 || response.status() >= 300;
    }

    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    public ResponseEntity<?> find(Long id) {
        Optional<Employee> employeeOptional = employeeRepository.findById(id);
        if (employeeOptional.isPresent()) {
            return ResponseEntity.ok(employeeOptional.get());
        }
        return ResponseEntity.status(404).body("Employee Not Found");
    }

    public ResponseEntity<?> create(Employee employee) {
        if (nonexistentDepartment(employee.getDepartmentId()))
            return ResponseEntity
                    .status(400)
                    .body(String.format("Department with id %d not found!", employee.getDepartmentId()));
        return ResponseEntity.ok(employeeRepository.save(employee));
    }

    public ResponseEntity<?> update(Long originalEmployeeId, Employee editedEmployee) {
        Optional<Employee> employeeOptional = employeeRepository.findById(originalEmployeeId);
        if (!employeeOptional.isPresent()) {
            return ResponseEntity.status(404).body("Employee Not Found");
        }
        if (editedEmployee == null) return ResponseEntity.status(400).body("Employee data is not valid!");
        Employee employeeToSave = employeeOptional.get();
        if (employeeToSave.getDepartmentId() != editedEmployee.getDepartmentId()) {
            if (nonexistentDepartment(editedEmployee.getDepartmentId())) {
                return ResponseEntity
                        .status(400)
                        .body(String.format("Department with id %d not found!", editedEmployee.getDepartmentId()));
            }
        }
        BeanUtils.copyProperties(editedEmployee, employeeToSave, "id");
        return ResponseEntity.ok(employeeRepository.save(employeeToSave));
    }

    public ResponseEntity<?> replaceDepartmentId(DepartmentsToReplaceDto departments) {
        List<Employee> employeesWithOldDepartmentId = employeeRepository.findByDepartmentId(departments.getOldDepartmentID());
        if (nonexistentDepartment(departments.getNewDepartmentID())) {
            return ResponseEntity
                    .status(400)
                    .body(String.format("Department with id %d not found!", departments.getNewDepartmentID()));
        }
        AtomicLong affectedEmployeeCount = new AtomicLong(0);
        employeesWithOldDepartmentId.forEach(employee -> {
            employee.setDepartmentId(departments.getNewDepartmentID());
            employeeRepository.save(employee);
            affectedEmployeeCount.incrementAndGet();
        });
        return ResponseEntity.ok(affectedEmployeeCount.get() + " employee(s) are affected");
    }

    public ResponseEntity<?> delete(Long id) {
        Optional<Employee> employeeOptional = employeeRepository.findById(id);
        if (!employeeOptional.isPresent()) {
            return ResponseEntity.status(404).body("Employee Not Found");
        }
        employeeRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
