package ru.xfneo.employeesmanagement.client;

import feign.Response;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

public interface DepartmentClient {

    @GetMapping(value = "/api/departments/{id}")
    Response checkDepartment(@PathVariable("id") long id);

}
