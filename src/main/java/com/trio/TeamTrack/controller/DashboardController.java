package com.trio.TeamTrack.controller;

import com.trio.TeamTrack.entity.Project;
import com.trio.TeamTrack.entity.User;
import com.trio.TeamTrack.enums.TaskStatus;
import com.trio.TeamTrack.service.ProjectService;
import com.trio.TeamTrack.service.StatisticsService;
import com.trio.TeamTrack.service.UserService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DashboardController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }
    @GetMapping("/dashboard")
    @Transactional
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        List<Project> projects = projectService.findAllForUser(user);

        // Calcul des stats globales pour le dashboard
        int totalTaches = 0;
        int tachesTerminees = 0;
        int projetsEnRetard = 0;

        for (Project p : projects) {
            totalTaches      += p.getTasks().size();
            tachesTerminees  += p.countByStatus(TaskStatus.DONE);
            if (p.isOverdue()) projetsEnRetard++;
        }

        model.addAttribute("user", user);
        model.addAttribute("projects", projects);
        model.addAttribute("totalProjects", projects.size());
        model.addAttribute("totalTaches", totalTaches);
        model.addAttribute("tachesTerminees", tachesTerminees);
        model.addAttribute("projetsEnRetard", projetsEnRetard);

        return "dashboard";
    }
}
