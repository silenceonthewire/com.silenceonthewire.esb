package repositories.users;

import com.google.common.collect.ImmutableMap;
import defaultData.DefaultCompanies;
import models.users.Company;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.Application;
import play.db.Database;
import play.db.Databases;
import play.db.evolutions.Evolutions;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.WithApplication;

import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.inMemoryDatabase;
import static play.test.Helpers.running;

public class GetCompanyByNameTest extends WithApplication {

    private DefaultCompanies defaultCompanies = new DefaultCompanies();

    private String existsName = "Example1";
    private String notExistsName = "Example3";

    Database database;

    @Before
    public void setUp() throws Exception {
        running(fakeApplication(inMemoryDatabase("test")), () -> {

            database = Databases.inMemory(
                    "test",
                    ImmutableMap.of(
                            "MODE", "MYSQL"
                    ),
                    ImmutableMap.of(
                            "logStatements", true
                    )
            );
            Evolutions.applyEvolutions(database);

            defaultCompanies.createCompanies();
        });
    }

    @After
    public void tearDown() throws Exception {
        running(fakeApplication(inMemoryDatabase("test")), () -> {

            defaultCompanies.deleteCompanies();

            Evolutions.cleanupEvolutions(database);
            database.shutdown();
        });
    }

    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder().build();
    }

    @Test
    public void getCompanyWithExistingName(){

        running(fakeApplication(inMemoryDatabase("test")), () -> {

            final CompanyRepository companyRepository = app.injector().instanceOf(CompanyRepository.class);
            final CompletionStage<Optional<Company>> stage = companyRepository.getByName(existsName);

            await().atMost(1, TimeUnit.SECONDS).until(() -> {

                assertThat(stage.toCompletableFuture()).isCompletedWithValueMatching(company -> {

                    return company.isPresent();
                });
            });
        });
    }

    @Test
    public void getCompanyWithNotExistsName(){

        running(fakeApplication(inMemoryDatabase("test")), () -> {

            final CompanyRepository companyRepository = app.injector().instanceOf(CompanyRepository.class);
            final CompletionStage<Optional<Company>> stage = companyRepository.getByName(notExistsName);

            await().atMost(1, TimeUnit.SECONDS).until(() -> {

                assertThat(stage.toCompletableFuture()).isCompletedWithValueMatching(company -> {

                    return !company.isPresent();
                });
            });
        });
    }
}
