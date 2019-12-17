package ru.xfneo.employeesmanagement.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.xfneo.employeesmanagement.model.DepartmentsToReplaceDto;
import ru.xfneo.employeesmanagement.model.Employee;
import ru.xfneo.employeesmanagement.service.EmployeeService;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@Api(value = "/api", tags = "Employees API")
public class EmployeesController {
    private final EmployeeService employeeService;

    @Autowired
    public EmployeesController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @ApiOperation(value = "Retrieve a list of all employees", response = Employee.class, responseContainer="List")
    @ApiResponse(code = 200, message = "Successfully retrieved list")
    @GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<Employee> getListOfEmployees(){
        return employeeService.findAll();
    }

    @ApiOperation(value = "Retrieve employee", response = Employee.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved employee"),
            @ApiResponse(code = 404, message = "Employee not found")
    })
    @GetMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> getEmployee(@PathVariable("id") Long id){
        return employeeService.find(id);
    }

    @ApiOperation(value = "Create employee", code = 201, response = Employee.class)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created employee"),
            @ApiResponse(code = 400, message = "Department not found")
    })
    @PostMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseStatus(code = HttpStatus.CREATED)
    public ResponseEntity<?> createEmployee(@RequestBody Employee employee){
        return employeeService.create(employee);
    }

    @ApiOperation(value = "Update employee", response = Employee.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully updated employee"),
            @ApiResponse(code = 400, message = "Employee data is not valid or new department not found"),
            @ApiResponse(code = 404, message = "Employee for update not found")
    })
    @PutMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> updateEmployee(
            @PathVariable("id") Long originalEmployeeId,
            @RequestBody Employee editedEmployee
    ){
        return employeeService.update(originalEmployeeId, editedEmployee);
    }

    @ApiOperation(value = "Transfer all employees from one department to another", response = String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully transferred employees"),
            @ApiResponse(code = 400, message = "New department not found")
    })
    @PostMapping(value = "/replaceDepartment", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> replaceDepartmentId(@RequestBody DepartmentsToReplaceDto departments){
        return employeeService.replaceDepartmentId(departments);
    }

    @ApiOperation(value = "Delete employee", code = 204)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successfully deleted employee or employee does not exist"),
    })
    @DeleteMapping(value = "{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public ResponseEntity<?> deleteEmployee(@PathVariable("id") Long id){
        return employeeService.delete(id);
    }
}