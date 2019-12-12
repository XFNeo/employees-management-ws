package ru.xfneo.employeesmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.xfneo.employeesmanagement.model.Employee;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByDepartmentId(long departmentId);
}
