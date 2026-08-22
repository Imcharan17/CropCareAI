package com.cropportal.config;

import com.cropportal.entity.*;
import com.cropportal.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final FarmerRepository farmerRepository;
    private final DoctorRepository doctorRepository;
    private final CropRepository cropRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        for (RoleName roleName : RoleName.values()) {
            roleRepository.findByName(roleName).orElseGet(() -> {
                Role role = new Role();
                role.setName(roleName);
                return roleRepository.save(role);
            });
        }
        seedAdmin();
        seedDoctor();
        seedFarmer();
        if (cropRepository.count() == 0) {
            for (String name : List.of("Rice", "Wheat", "Tomato", "Potato", "Cotton")) {
                Crop crop = new Crop();
                crop.setName(name);
                crop.setSeason("All season");
                crop.setDescription("Managed crop category for " + name);
                cropRepository.save(crop);
            }
        }
    }

    private void seedAdmin() {
        if (userRepository.existsByEmail("admin@crop.ai")) return;
        User user = base("Platform Admin", "admin@crop.ai", RoleName.ROLE_ADMIN);
        userRepository.save(user);
    }

    private void seedDoctor() {
        if (userRepository.existsByEmail("doctor@crop.ai")) return;
        User user = userRepository.save(base("Dr. Asha Verma", "doctor@crop.ai", RoleName.ROLE_DOCTOR));
        Doctor doctor = new Doctor();
        doctor.setUser(user);
        doctor.setSpecialization("Plant pathology");
        doctor.setLicenseNumber("AGR-2045");
        doctor.setExperienceYears(9);
        doctorRepository.save(doctor);
    }

    private void seedFarmer() {
        if (userRepository.existsByEmail("farmer@crop.ai")) return;
        User user = userRepository.save(base("Ravi Kumar", "farmer@crop.ai", RoleName.ROLE_FARMER));
        Farmer farmer = new Farmer();
        farmer.setUser(user);
        farmer.setFarmLocation("Mysuru, Karnataka");
        farmer.setPrimaryCrop("Tomato");
        farmer.setFarmSizeAcres(4.5);
        farmerRepository.save(farmer);
    }

    private User base(String name, String email, RoleName roleName) {
        User user = new User();
        user.setFullName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("Password@123"));
        user.setRoles(Set.of(roleRepository.findByName(roleName).orElseThrow()));
        return user;
    }
}
