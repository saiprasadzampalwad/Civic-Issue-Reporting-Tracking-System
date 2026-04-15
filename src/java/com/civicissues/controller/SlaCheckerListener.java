/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.civicissues.controller;

import com.civicissues.dao.IssueDAO;
import com.civicissues.model.Issue;


import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * SlaCheckerServlet.java — Background timer that checks for SLA breaches every 6 hours.
 * Implements ServletContextListener so it starts when the app deploys and stops on undeploy.
 *
 * Configure ADMIN_EMAIL below with the municipal admin's address.
 */
@WebListener
public class SlaCheckerListener implements ServletContextListener {

    private static final String ADMIN_EMAIL      = "admin@civic.gov"; // <-- change
    private static final long   CHECK_INTERVAL_MS = 6 * 60 * 60 * 1000L; // 6 hours

    private Timer timer;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        timer = new Timer("SlaChecker", true); // daemon thread
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkSlaBreaches();
            }
        }, 60_000L, CHECK_INTERVAL_MS); // first check 1 min after startup
        System.out.println("[SlaChecker] SLA breach checker started (every 6 hours).");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (timer != null) { timer.cancel(); }
        System.out.println("[SlaChecker] SLA breach checker stopped.");
    }

    private void checkSlaBreaches() {
        try {
            IssueDAO dao = new IssueDAO();
            List<Issue> breached = dao.getSlaBreachedIssues();
            if (breached.isEmpty()) {
                System.out.println("[SlaChecker] No SLA breaches found.");
                return;
            }
            System.out.println("[SlaChecker] Found " + breached.size() + " SLA breach(es). Sending alert.");
            for (Issue issue : breached) {
                long hoursOpen = 0;
                if (issue.getCreatedAt() != null) {
                    hoursOpen = (System.currentTimeMillis() - issue.getCreatedAt().getTime()) / (1000 * 60 * 60);
                }
                System.out.println("[Simulated SLA Alert] to " + ADMIN_EMAIL + " for issue #" + issue.getIssueId() + " (" + issue.getCategory() + ") overdue " + hoursOpen + "h");
            }
        } catch (Exception e) {
            System.err.println("[SlaChecker] Error: " + e.getMessage());
        }
    }
}