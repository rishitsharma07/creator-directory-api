package com.digitace.creator_directory_api.config;

import com.digitace.creator_directory_api.domain.*;
import com.digitace.creator_directory_api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final AgencyRepository agencyRepository;
    private final UserRepository userRepository;
    private final CreatorRepository creatorRepository;
    private final AgencyCreatorLinkRepository linkRepository;

    @Override
    public void run(String... args) {
        // If data already exists, do not run the seeder again
        if (agencyRepository.count() > 0) return;

        // 1. Create the Tenants
        Agency nova = agencyRepository.save(Agency.builder().name("Nova Talent").plan(PlanType.FREE).build());
        Agency brightStar = agencyRepository.save(Agency.builder().name("Bright Star Agency").plan(PlanType.PRO).build());

        // 2. Create the Users (Letting the DB generate the UUIDs this time)
        User novaUser = userRepository.save(User.builder().agency(nova).email("owner@nova.com").role(RoleType.OWNER).build());
        User brightStarUser = userRepository.save(User.builder().agency(brightStar).email("owner@brightstar.com").role(RoleType.OWNER).build());

        // 3. Create the Core Creators
        Creator priya = creatorRepository.save(Creator.builder().name("Priya Sharma").niche("beauty").followerCount(45000).engagementRate(3.8).email("priya@example.com").build());
        Creator rahul = creatorRepository.save(Creator.builder().name("Rahul Verma").niche("fitness").followerCount(120000).engagementRate(2.1).email("rahul@example.com").build());

        // 4. The Isolation Test: Both agencies link to Priya, but with DIFFERENT private notes
        linkRepository.save(AgencyCreatorLink.builder().agency(nova).creator(priya).notes("Great for skincare campaigns").build());
        linkRepository.save(AgencyCreatorLink.builder().agency(brightStar).creator(priya).notes("Booked for Q1 shoot").build());

        // Only Bright Star links to Rahul
        linkRepository.save(AgencyCreatorLink.builder().agency(brightStar).creator(rahul).notes("High reach, slower replies").build());

        System.out.println("\n=========================================================");
        System.out.println("DATABASE SEEDED SUCCESSFULLY.");
        System.out.println("TEST NOVA TALENT (Header)   -> X-User-Id: " + novaUser.getId());
        System.out.println("TEST BRIGHT STAR (Header)   -> X-User-Id: " + brightStarUser.getId());
        System.out.println("=========================================================\n");
    }
}