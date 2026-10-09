package com.mamadou.sutura.ledger;

import com.mamadou.sutura.ledger.entity.LedgerTrace;
import java.util.UUID;
import java.util.concurrent.*;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.*;

class LedgerReservationTest {
    private SessionFactory factory;

    @BeforeEach void start() {
        factory = new Configuration().addAnnotatedClass(LedgerTrace.class)
                .setProperty("hibernate.connection.driver_class", "org.h2.Driver")
                .setProperty("hibernate.connection.url", "jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=5000")
                .setProperty("hibernate.hbm2ddl.auto", "create-drop")
                .buildSessionFactory();
    }

    @AfterEach void close() { factory.close(); }

    @Test void concurrentReservationAllowsOnlyOneCommit() throws Exception {
        var executor = Executors.newFixedThreadPool(2);
        var start = new CountDownLatch(1);
        Callable<Boolean> reserve = () -> {
            start.await();
            try (var session = factory.openSession()) {
                var tx = session.beginTransaction();
                try {
                    session.persist(new LedgerTrace("same-transfer", "GMD"));
                    session.flush();
                    tx.commit();
                    return true;
                } catch (RuntimeException e) {
                    tx.rollback();
                    return false;
                }
            }
        };
        try {
            var first = executor.submit(reserve);
            var second = executor.submit(reserve);
            start.countDown();
            int commits = (first.get(10, TimeUnit.SECONDS) ? 1 : 0)
                    + (second.get(10, TimeUnit.SECONDS) ? 1 : 0);
            assertThat(commits).isEqualTo(1);
            try (var session = factory.openSession()) {
                assertThat(session.createQuery("select count(t) from LedgerTrace t", Long.class).getSingleResult()).isEqualTo(1L);
            }
        } finally { executor.shutdownNow(); }
    }

    @Test void rollbackReleasesReservationForRetry() {
        try (var session = factory.openSession()) {
            var tx = session.beginTransaction();
            session.persist(new LedgerTrace("retry-transfer", "GMD"));
            session.flush();
            tx.rollback();
        }
        try (var session = factory.openSession()) {
            var tx = session.beginTransaction();
            assertThat(session.find(LedgerTrace.class, "retry-transfer")).isNull();
            session.persist(new LedgerTrace("retry-transfer", "GMD"));
            tx.commit();
        }
    }
}
