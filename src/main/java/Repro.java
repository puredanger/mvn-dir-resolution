import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.*;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectModelResolver;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositoryCache;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.internal.impl.DefaultRemoteRepositoryManager;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.LocalRepositoryManager;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Repro {

    public static void main(String[] args) throws Throwable {
        DefaultServiceLocator loc = MavenRepositorySystemUtils.newServiceLocator();
        loc.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        loc.addService(TransporterFactory.class, FileTransporterFactory.class);
        loc.addService(TransporterFactory.class, HttpTransporterFactory.class);

        RepositorySystem system = loc.getService(RepositorySystem.class);
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
        String local = System.getProperty("user.home") + "/.m2/repository";
        LocalRepositoryManager localRepoMgr = system.newLocalRepositoryManager(session, new LocalRepository(local));
        session.setLocalRepositoryManager(localRepoMgr);
        session.setCache(new DefaultRepositoryCache());

        DefaultRemoteRepositoryManager repoMgr = new DefaultRemoteRepositoryManager();
        repoMgr.initService(loc);
        List<RemoteRepository> repos = new ArrayList<RemoteRepository>();

        ProjectModelResolver resolver = new ProjectModelResolver(session, null, system, repoMgr, repos, ProjectBuildingRequest.RepositoryMerging.REQUEST_DOMINANT, null);
        FileModelSource source = new FileModelSource(new File("./pom.xml"));
        Properties props = new Properties();
        props.putAll(System.getProperties());
        props.setProperty("project.basedir", ".");

        DefaultModelBuildingRequest req = new DefaultModelBuildingRequest()
                .setModelSource(source)
                .setModelResolver(resolver)
                .setSystemProperties(props);
        DefaultModelBuilderFactory factory = new DefaultModelBuilderFactory();
        DefaultModelBuilder builder = factory.newInstance();
        ModelBuildingResult result = builder.build(req);
        Model model = result.getEffectiveModel();
        Build build = model.getBuild();
        String srcDir = build.getSourceDirectory();
        File srcDirFile = new File(srcDir);

        System.out.println("Build.getSourceDirectory() javadoc says \"The path given is relative to the project descriptor.\" but:");
        System.out.println("srcDir= " + srcDir + " absolute=" + srcDirFile.isAbsolute());
    }
}
