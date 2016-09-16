package nablarch.codefirst.control;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

/**
 * Oracle SQL*Loaderのコントロールファイル生成用Mavenプラグイン。
 *
 * @author Hisaaki Shioiri
 */
@Mojo(name = "generate-ctrl-file", requiresDependencyResolution = ResolutionScope.RUNTIME)
public class ControlFileGeneratorMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    /** 出力先のディレクトリ */
    @Parameter(defaultValue = "${project.build.directory}/etl/ctrl-file")
    File outputPath;

    /**
     * テンプレートファイルのパス。
     * <p/>
     * テンプレートファイルは、「nablarch/codefirst/control」からのパスを設定する。
     * 例えば、テンプレートファイルのパスがnablarch/codefirst/control/custom/template.ftlの場合には、custom/template.ftlを設定する。
     * <p/>
     * テンプレートファイルは、クラスパス配下から検索する。
     */
    @Parameter(defaultValue = "template/template.ftl")
    String templateFilePath;

    /**
     * コントロールファイルの生成対象となるクラスの完全修飾名リスト
     */
    @Parameter(required = true)
    List<String> classes;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        final ClassLoader newLoader = createClassLoader();

        if (!outputPath.exists() && !outputPath.mkdirs()) {
            throw new IllegalArgumentException("出力先ディレクトリの作成に失敗しました。");
        }

        final ControlFileGenerator generator = new ControlFileGenerator();
        for (String fqcn : classes) {
            try {
                generator.execute(newLoader.loadClass(fqcn), templateFilePath, outputPath);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("指定されたBeanクラスが存在しません。 Beanクラス=[" + fqcn + ']', e);
            }
        }
    }

    /**
     * プロジェクトのクラスパスを元にクラスローダを生成する。
     *
     * @return クラスローダ
     */
    private ClassLoader createClassLoader() {
        URLClassLoader newLoader;
        try {
            final List<String> elements = project.getRuntimeClasspathElements();
            final URL[] urls = new URL[elements.size()];
            for (int i = 0; i < elements.size(); i++) {
                String element = elements.get(i);
                urls[i] = new File(element).toURI().toURL();
            }
            newLoader = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
        } catch (DependencyResolutionRequiredException e) {
            throw new IllegalArgumentException(e);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
        return newLoader;
    }
}
