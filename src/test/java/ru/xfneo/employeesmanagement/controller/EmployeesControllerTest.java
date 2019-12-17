package ru.xfneo.employeesmanagement.controller;

import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.xfneo.employeesmanagement.model.DepartmentsToReplaceDto;
import ru.xfneo.employeesmanagement.model.Employee;
import ru.xfneo.employeesmanagement.service.EmployeeService;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@RunWith(MockitoJUnitRunner.class)
public class EmployeesControllerTest {
    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeesController sut;

    private MockMvc mockMvc;
    private Employee employee1;
    private Employee employee2;
    private Employee employee1UpdatedDepartment;

    private static final String GET_ALL_EMPLOYEES_URI = "/api/employees";
    private static final String POST_EMPLOYEE_URI = "/api/employees";
    private static final String GET_PUT_DELETE_EMPLOYEE_URI = "/api/employees/1";
    private static final String POST_REPLACE_DEPARTMENT_URI = "/api/employees/replaceDepartment";
    private static final String CREATE_EMPLOYEE1_JSON =
            "{\"departmentId\":1,\"firstName\":\"firstName1\",\"lastName\":\"lastName1\",\"title\":\"title1\"}";
    private static final String UPDATE_EMPLOYEE1_JSON =
            "{\"departmentId\":2,\"firstName\":\"firstName1\",\"lastName\":\"lastName1\",\"title\":\"title1\"}";
    private static final String REPLACE_DEPARTMENT_JSON =
            "{\"newDepartmentID\":1,\"oldDepartmentID\":2}";

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(sut).build();
        employee1 = new Employee(1, 1, "firstName1", "lastName1", "title1");
        employee2 = new Employee(2, 2, "firstName2", "lastName2", "title2");
        employee1UpdatedDepartment = new Employee(1, 2, "firstName1", "lastName1", "title1");
        List<Employee> findAllList = Arrays.asList(employee1, employee2);
        doReturn(findAllList).when(employeeService).findAll();
        doReturn(ResponseEntity.ok(employee1)).when(employeeService).find(employee1.getId());
        doReturn(ResponseEntity.ok(employee1)).when(employeeService).create(any(Employee.class));
        doReturn(ResponseEntity.ok(employee1UpdatedDepartment)).when(employeeService).update(eq(employee1.getId()), any(Employee.class));
        doReturn(ResponseEntity.ok().build()).when(employeeService).replaceDepartmentId(any(DepartmentsToReplaceDto.class));
        doReturn(ResponseEntity.ok().build()).when(employeeService).delete(employee1.getId());
    }

    @Test
    @SneakyThrows
    public void testGetListOfEmployees() {
        mockMvc.perform(get(GET_ALL_EMPLOYEES_URI))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$[0].id", is((int) employee1.getId())))
                .andExpect(jsonPath("$[0].departmentId", is((int) employee1.getDepartmentId())))
                .andExpect(jsonPath("$[0].firstName", is(employee1.getFirstName())))
                .andExpect(jsonPath("$[0].lastName", is(employee1.getLastName())))
                .andExpect(jsonPath("$[0].title", is(employee1.getTitle())))
                .andExpect(jsonPath("$[1].id", is((int) employee2.getId())))
                .andExpect(jsonPath("$[1].departmentId", is((int) employee2.getDepartmentId())))
                .andExpect(jsonPath("$[1].firstName", is(employee2.getFirstName())))
                .andExpect(jsonPath("$[1].lastName", is(employee2.getLastName())))
                .andExpect(jsonPath("$[1].title", is(employee2.getTitle())));
        verify(employeeService).findAll();
    }

    @Test
    @SneakyThrows
    public void testGetEmployee() {
        mockMvc.perform(get(GET_PUT_DELETE_EMPLOYEE_URI))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.id", is((int) employee1.getId())))
                .andExpect(jsonPath("$.departmentId", is((int) employee1.getDepartmentId())))
                .andExpect(jsonPath("$.firstName", is(employee1.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(employee1.getLastName())))
                .andExpect(jsonPath("$.title", is(employee1.getTitle())));
        verify(employeeService).find(employee1.getId());
    }

    @Test
    @SneakyThrows
    public void testCreateEmployee() {
        mockMvc.perform(post(POST_EMPLOYEE_URI)
                    .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                    .content(CREATE_EMPLOYEE1_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.id", is((int) employee1.getId())))
                .andExpect(jsonPath("$.departmentId", is((int) employee1.getDepartmentId())))
                .andExpect(jsonPath("$.firstName", is(employee1.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(employee1.getLastName())))
                .andExpect(jsonPath("$.title", is(employee1.getTitle())));
        verify(employeeService).create(any(Employee.class));
    }

    @Test
    @SneakyThrows
    public void testUpdateEmployee() {
        mockMvc.perform(put(GET_PUT_DELETE_EMPLOYEE_URI)
                    .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                    .content(UPDATE_EMPLOYEE1_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.id", is((int) employee1UpdatedDepartment.getId())))
                .andExpect(jsonPath("$.departmentId", is((int) employee1UpdatedDepartment.getDepartmentId())))
                .andExpect(jsonPath("$.firstName", is(employee1UpdatedDepartment.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(employee1UpdatedDepartment.getLastName())))
                .andExpect(jsonPath("$.title", is(employee1UpdatedDepartment.getTitle())));
        verify(employeeService).update(eq(employee1.getId()), any(Employee.class));
    }

    @Test
    @SneakyThrows
    public void testReplaceDepartmentId() {
        mockMvc.perform(post(POST_REPLACE_DEPARTMENT_URI)
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .content(REPLACE_DEPARTMENT_JSON))
                .andDo(print())
                .andExpect(status().isOk());
        verify(employeeService).replaceDepartmentId(any(DepartmentsToReplaceDto.class));
    }

    @Test
    @SneakyThrows
    public void testDeleteEmployee() {
        mockMvc.perform(delete(GET_PUT_DELETE_EMPLOYEE_URI))
                .andDo(print())
                .andExpect(status().isOk());
        verify(employeeService).delete(employee1.getId());
    }
}