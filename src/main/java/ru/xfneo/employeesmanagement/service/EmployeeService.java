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

    /**
     * Get all existing employees from repository
     *
     * @return List of all existing employees from repository.
     */
    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    /**
     * Get the employee from repository.
     *
     * @param id  ID of the employee you want to receive
     * @return ResponseEntity with code 200 and employee in body,
     * or ResponseEntity with code 404 and body "Employee Not Found" if employee with that id does not exist.
     */
    public ResponseEntity<?> find(Long id) {
        Optional<Employee> employeeOptional = employeeRepository.findById(id);
        if (employeeOptional.isPresent()) {
            return ResponseEntity.ok(employeeOptional.get());
        }
        return ResponseEntity.status(404).body("Employee Not Found");
    }

    /**
     * Create and save the employee to repository,
     * checking before that the specified department exists
     * (call external service department-management-ws).
     *
     * @param employee  employee to check and save
     * @return ResponseEntity with code 201 and saved employee in body,
     * or ResponseEntity with code 400 and body "Department with id %d not found!" if department does not exist.
     */
    public ResponseEntity<?> create(Employee employee) {
        if (nonexistentDepartment(employee.getDepartmentId()))
            return ResponseEntity
                    .status(400)
                    .body(String.format("Department with id %d not found!", employee.getDepartmentId()));
        return ResponseEntity.status(201).body(employeeRepository.save(employee));
    }

    /**
     * Update the employee and save to repository,
     * if department changed, checking before that the specified department exists
     * (call external service department-management-ws).
     *
     * @param originalEmployeeId  id of the employee to change
     * @param editedEmployee  updated employee's data to save
     * @return ResponseEntity with code 200 and updated employee in body,
     * or ResponseEntity with code 404 and body "Employee Not Found" if original employee does not exist,
     * or ResponseEntity with code 400 and body "Employee data is not valid!" if updated employee is null,
     * or ResponseEntity with code 400 and body "Department with id %d not found!" if edited department does not exist.
     */
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

    /**
     * Transfer all employees from one department to another and save them to repository,
     * checking before that new department exists
     * (call external service department-management-ws).
     *
     * @param departments class with 2 department ids: old department and new department
     * @return ResponseEntity with code 200 and body "%d employee(s) are affected"(how many employees have been transferred),
     * or ResponseEntity with code 400 and body "Department with id %d not found!" if new department does not exist.
     */
    public ResponseEntity<?> replaceDepartmentId(DepartmentsToReplaceDto departments) {
        List<Employee> employeesWithOldDepartmentId = employeeRepository.findByDepartmentId(departments.getOldDepartmentID());
        if (nonexistentDepartment(departments.getNewDepartmentID())) {
            return ResponseEntity
                    .status(400)
                    .body(String.format("Department with id %d not found!", departments.getNewDepartmentID()));
        }
        employeesWithOldDepartmentId.forEach(employee -> {
            employee.setDepartmentId(departments.getNewDepartmentID());
            employeeRepository.save(employee);
        });
        return ResponseEntity.ok(employeesWithOldDepartmentId.size() + " employee(s) are affected");
    }

    /**
     * Delete the employee from repository.
     *
     * @param id id of the employee to delete
     * @return ResponseEntity with code 204 without body.
     */
    public ResponseEntity<?> delete(Long id) {
        Optional<Employee> employeeOptional = employeeRepository.findById(id);
        if (employeeOptional.isPresent()) {
            employeeRepository.deleteById(id);
        }
        return ResponseEntity.status(204).build();
    }
}
