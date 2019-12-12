package ru.xfneo.employeesmanagement.model;

import lombok.Data;

@Data
public class DepartmentsToReplaceDto {
    private long oldDepartmentID;
    private long newDepartmentID;
}