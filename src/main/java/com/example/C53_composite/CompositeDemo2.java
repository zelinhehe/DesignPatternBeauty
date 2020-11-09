package com.example.C53_composite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CompositeDemo2 {
    public static void main(String[] args) {
        // 所有员工的 salary之和
        Organization organization = new Organization(Organization.ORGANIZATION_ROOT_ID);
        double salary = organization.getDepartment().calculateSalary();
        System.out.println(salary);

        // 部门1003L的所有员工的 salary之和
        organization = new Organization(1003L);
        salary = organization.getDepartment().calculateSalary();
        System.out.println(salary);
    }
}

abstract class HumanResource {
    protected long id;
    protected double salary;

    public HumanResource(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public abstract double calculateSalary();
}

class Employee extends HumanResource {
    public Employee(long id, double salary) {
        super(id);
        this.salary = salary;
    }

    @Override
    public double calculateSalary() {
        return salary;
    }
}

class Department extends HumanResource {
    private final List<HumanResource> subNodes = new ArrayList<>();

    public Department(long id) {
        super(id);
    }

    @Override
    public double calculateSalary() {
        double totalSalary = 0;
        for (HumanResource hr : subNodes) {
            totalSalary += hr.calculateSalary();
        }
        this.salary = totalSalary;
        return totalSalary;
    }

    public void addSubNode(HumanResource hr) {
        subNodes.add(hr);
    }
}

// 构建组织架构的代码
class Organization {
    public static final long ORGANIZATION_ROOT_ID = 1001;
    private final Department department;

    public Department getDepartment() {
        return department;
    }

    public Organization(Long departmentId) {
        department = new Department(departmentId);
        buildOrganization(department);
    }

    private void buildOrganization(Department department) {
        List<Long> subDepartmentIds = getSubDepartmentIds(department.getId());
        for (Long subDepartmentId : subDepartmentIds) {
            Department subDepartment = new Department(subDepartmentId);
            department.addSubNode(subDepartment);
            buildOrganization(subDepartment);
        }
        List<Long> employeeIds = getSubEmployeeIds(department.getId());
        for (Long employeeId : employeeIds) {
            double salary = employeeId;
            department.addSubNode(new Employee(employeeId, salary));
        }
    }

    /**
     * 部门和员工的关系树。[部门]  员工
     *
     *                       [1001]
     *            [1002]               [1003]
     * [1004] [1005] 10021 10022     10031 10032
     *  10041
     *
     */
    private List<Long> getSubDepartmentIds(Long id) {
        if (id == ORGANIZATION_ROOT_ID) {
            return new ArrayList<>(Arrays.asList(1002L, 1003L));
        } else if (id == 1002L) {
            return new ArrayList<>(Arrays.asList(1004L, 1005L));
        } else {
            return Collections.emptyList();
        }
    }

    private List<Long> getSubEmployeeIds(Long id) {
        if (id == 1002L) {
            return new ArrayList<>(Arrays.asList(10021L, 10022L));
        } else if (id == 1003L) {
            return new ArrayList<>(Arrays.asList(10031L, 10032L));
        } else if (id == 1004L) {
            return new ArrayList<>(Arrays.asList(10041L));
        } else {
            return Collections.emptyList();
        }
    }
}
