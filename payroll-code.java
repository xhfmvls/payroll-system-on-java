package payrollSystem;
import java.util.Scanner;
import java.util.Vector;
import java.security.*;
import java.util.Date;  

class Employees {
	static Vector<Employee> Employees = new Vector<>(); 
}

class DailySchedule {
	public int dayDate;
	public int ClockIn; 
	public int ClockOut;
	public int hourStarted; 
	public int hourEnded; 
	public int lateHour;
	public Boolean haveClockIn; 
	public DailySchedule(int dayDate) {
		this.dayDate = dayDate; 
		this.hourStarted = 8;
		this.hourEnded = 17; 
		this.haveClockIn = false;
	}
}

class MonthlyCalendar {
	public Vector<DailySchedule> monthlySchedule = new Vector<>(); 
	public MonthlyCalendar() {
		for(int i = 1; i <= 31; i++) {
			monthlySchedule.add(new DailySchedule(i)); 
		}
	}
}

class Employee {
	public String emailAddress; 
	public String hashedPassword;
	public int basicSalary; 
	public MonthlyCalendar Calendar; 
	public int penalty; 
	public int overtimePay; 
	public int weeklyOvertimeRemain; 
	public Employee(String emailAddress, String hashedPassword) {
		this.emailAddress = emailAddress; 
		this.hashedPassword = hashedPassword;
		this.basicSalary = 1000000; 
		this.overtimePay = 25000; 
		this.penalty = 75000;
		this.weeklyOvertimeRemain = 10; 
		Calendar = new MonthlyCalendar(); 
	}
}

class Login {
	Vector<Employee> employeeList = Employees.Employees;
	
	public Employee main(String emailAddress, String unhashedPassword) {
		String hashedPassword = hash(unhashedPassword); 
		for (Employee employee : employeeList) {
			if(employee.emailAddress.equals(emailAddress) && employee.hashedPassword.equals(hashedPassword)) {
				return employee; 
			}
		} 
		return null; 
	}
	public String hash(String str) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		md.update(str.getBytes());
		
		byte[] digest = md.digest(); 
		StringBuffer sb = new StringBuffer(); 
		for(byte b: digest) {
			sb.append(String.format("%02x", b & 0xff));
		}
		return sb.toString(); 
	}
}

class Attendance {
	public void attend(Employee employee, int date, int hour) {
		DailySchedule schedule = employee.Calendar.monthlySchedule.get(date - 1); 
		if(schedule.haveClockIn == false) {
			schedule.haveClockIn = true; 
			schedule.ClockIn = hour; 
			int lateHour = schedule.ClockIn - schedule.hourStarted + 1; 
			if(lateHour <= 0) {
				return; 
			}
			employee.basicSalary -= lateHour * employee.penalty; 
			return; 
		}
		schedule.ClockOut = hour; 
	}
}

class Overtime {
	public Boolean apply(Employee employee, int date, int requestedHour) {
		DailySchedule schedule = employee.Calendar.monthlySchedule.get(date - 1 + 7); 
		if(requestedHour > 4) {
			return false; 
		}
		if(employee.weeklyOvertimeRemain < requestedHour) {
			return false; 
		}
		employee.weeklyOvertimeRemain -= requestedHour; 
		schedule.hourEnded += requestedHour; 
		employee.basicSalary += (employee.overtimePay * requestedHour); 
		return true; 
	}
}

class Testing {
	public Boolean main() {
		if(loginTest("vincent.pradipta@binus.ac.id", "binusHebat") == false) {
			return false; 
		}
		if(attendanceTest("vincent.pradipta@binus.ac.id", "binusHebat") == false) {
			return false; 
		}
		if(overtimeTest("vincent.pradipta@binus.ac.id", "binusHebat") == false) {
			return false; 
		}
		return true; 
	}
	
	public Boolean loginTest(String emailAddress, String password) {
		Login login = new Login();
		Employee employee = login.main(emailAddress, password); 
		if(employee == null) {
			return false; 
		}
		return true; 
	}
	
	public Boolean attendanceTest(String emailAddress, String password) {
		Login login = new Login();
		Employee employee = login.main(emailAddress, password); 
		int date = 10; 
		Attendance attendance = new Attendance();
		
		attendance.attend(employee, date, 9); // clock in with two hour penalty
		attendance.attend(employee, date, 17); // clock out
		
		Integer test1 = employee.Calendar.monthlySchedule.get(date-1).ClockIn;
		Integer test2 = employee.Calendar.monthlySchedule.get(date-1).ClockOut; 
		Boolean haveAttend = employee.Calendar.monthlySchedule.get(date-1).haveClockIn; 
		Integer newSalary = employee.basicSalary; 
		
		
		if(test1 == 9 && test2 == 17 && newSalary == 1000000 - 75000 * 2 && haveAttend) {
			return true;
		}
		return false; 
	}
	
	public Boolean overtimeTest(String emailAddress, String password) {
		Login login = new Login(); 
		Employee employee = login.main(emailAddress, password); 
		int date = 10; 
		Overtime overtime = new Overtime(); 
		
		Boolean test1 = overtime.apply(employee, date, 7); // would return false as it exceed the daily time threshold 
		Boolean test2 = overtime.apply(employee, date + 1, 4); 
		Boolean test3 = overtime.apply(employee, date + 2, 3);
		Boolean test4 = overtime.apply(employee, date + 3, 4); // would return false as it exceed the weekly overtime limit
		int newSalary = employee.basicSalary;
		int newCheckoutTime = employee.Calendar.monthlySchedule.get(date + 7).hourEnded; 
		
		if(!test4 && test3 && test2 && !test1 && newSalary == 850000 + (25000 * 7) && newCheckoutTime == 21) {
			return true; 
		}
		return false; 
	}
}

public class PayrollClass {
	public static void main(String[] args) {
		Vector<Employee> employeeList = Employees.Employees;
		Employee employee1 = new Employee("vincent.pradipta@binus.ac.id", "c3f4bbad6b822185a540b9cb14960040ac94a15f621a72606e3761271df4a614"); 
		employeeList.add(employee1); 
		
		Testing test = new Testing();
		System.out.println("Login Test: " + test.main());
	}

}

