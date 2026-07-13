package com.LocalServe.LocalServe;

import com.LocalServe.LocalServe.entity.User;
import com.LocalServe.LocalServe.entity.Vendor;
import com.LocalServe.LocalServe.repository.UserRepository;
import com.LocalServe.LocalServe.repository.VendorRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class LocalServeApplication {

	public static void main(String[] args) {
		SpringApplication.run(LocalServeApplication.class, args);
	}

	@Bean
	public CommandLineRunner dataSeeder(UserRepository userRepository, VendorRepository vendorRepository) {
		return args -> {
			// 1. Seed System Admin
			if (!userRepository.existsByEmail("admin@gmail.com")) {
				User admin = User.builder()
						.fullName("System Admin")
						.email("admin@gmail.com")
						.password("admin")
						.phoneNumber("9999999999")
						.role("ADMIN")
						.build();
				userRepository.save(admin);
				System.out.println("Seeded admin user: admin@gmail.com / admin");
			}

			// 2. Seed Customer Account
			if (!userRepository.existsByEmail("customer@gmail.com")) {
				User customer = User.builder()
						.fullName("Arsha Begum")
						.email("customer@gmail.com")
						.password("customer")
						.phoneNumber("9962382506")
						.role("CUSTOMER")
						.build();
				userRepository.save(customer);
				System.out.println("Seeded customer user: customer@gmail.com / customer");
			}

			// 3. Seed Vendor 1: John Photography
			if (!userRepository.existsByEmail("john@gmail.com")) {
				User userJohn = User.builder()
						.fullName("John Doe")
						.email("john@gmail.com")
						.password("vendor")
						.phoneNumber("9876543210")
						.role("VENDOR")
						.build();
				User savedJohn = userRepository.save(userJohn);

				Vendor vendorJohn = Vendor.builder()
						.user(savedJohn)
						.businessName("John Photography")
						.serviceType("Photographer")
						.description("Professional wedding and event photography services. Capturing your beautiful moments.")
						.price(5000.0)
						.city("Salem")
						.availabilityStatus(true)
						.rating(5.0)
						.totalReviews(1)
						.build();
				vendorRepository.save(vendorJohn);
				System.out.println("Seeded vendor: john@gmail.com / vendor (John Photography)");
			}

			// 4. Seed Vendor 2: Royal Caterers
			if (!userRepository.existsByEmail("royal@gmail.com")) {
				User userRoyal = User.builder()
						.fullName("Sarah Smith")
						.email("royal@gmail.com")
						.password("vendor")
						.phoneNumber("8765432109")
						.role("VENDOR")
						.build();
				User savedRoyal = userRepository.save(userRoyal);

				Vendor vendorRoyal = Vendor.builder()
						.user(savedRoyal)
						.businessName("Royal Caterers")
						.serviceType("Caterer")
						.description("Delicious multi-cuisine catering services for weddings, corporate meets, and small events.")
						.price(15000.0)
						.city("Erode")
						.availabilityStatus(true)
						.rating(4.0)
						.totalReviews(1)
						.build();
				vendorRepository.save(vendorRoyal);
				System.out.println("Seeded vendor: royal@gmail.com / vendor (Royal Caterers)");
			}

			// 5. Seed Vendor 3: DJ Beats
			if (!userRepository.existsByEmail("djbeats@gmail.com")) {
				User userDj = User.builder()
						.fullName("David Johnson")
						.email("djbeats@gmail.com")
						.password("vendor")
						.phoneNumber("7654321098")
						.role("VENDOR")
						.build();
				User savedDj = userRepository.save(userDj);

				Vendor vendorDj = Vendor.builder()
						.user(savedDj)
						.businessName("DJ Beats")
						.serviceType("DJ")
						.description("Premium sound systems, lighting setups, and groovy music sets for parties and receptions.")
						.price(8000.0)
						.city("Coimbatore")
						.availabilityStatus(true)
						.rating(0.0)
						.totalReviews(0)
						.build();
				vendorRepository.save(vendorDj);
				System.out.println("Seeded vendor: djbeats@gmail.com / vendor (DJ Beats)");
			}
		};
	}
}