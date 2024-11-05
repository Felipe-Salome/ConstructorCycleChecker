package constructorcyclechecker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class ConstructorCycleCheckerTest {

    private ConstructorCycleChecker checker;

    @TempDir
    Path tempDir;

    @BeforeEach
    public void setup() {
        checker = new ConstructorCycleChecker();
    }

    @Test
    public void testVerificarCiclosConstrutores_semCiclo() throws IOException {
        criarArquivoClasse(tempDir, "ClasseA", "public class ClasseA { public ClasseA() { new ClasseB(); } }");
        criarArquivoClasse(tempDir, "ClasseB", "public class ClasseB { public ClasseB() { } }");

        checker.verificarCiclosConstrutores(tempDir);
        assertFalse(checker.temCicloDetectado(), "Nenhum ciclo deve ser detectado.");
    }

    @Test
    public void testVerificarCiclosConstrutores_comCicloSimples() throws IOException {
        criarArquivoClasse(tempDir, "ClasseA", "public class ClasseA { public ClasseA() { new ClasseB(); } }");
        criarArquivoClasse(tempDir, "ClasseB", "public class ClasseB { public ClasseB() { new ClasseA(); } }");

        checker.verificarCiclosConstrutores(tempDir);
        assertTrue(checker.temCicloDetectado(), "Ciclo deve ser detectado entre ClasseA e ClasseB.");
    }

    @Test
    public void testVerificarCiclosConstrutores_comCicloComplexo() throws IOException {
        criarArquivoClasse(tempDir, "ClasseA", "public class ClasseA { public ClasseA() { new ClasseB(); } }");
        criarArquivoClasse(tempDir, "ClasseB", "public class ClasseB { public ClasseB() { new ClasseC(); } }");
        criarArquivoClasse(tempDir, "ClasseC", "public class ClasseC { public ClasseC() { new ClasseA(); } }");

        checker.verificarCiclosConstrutores(tempDir);
        assertTrue(checker.temCicloDetectado(), "Ciclo complexo deve ser detectado entre ClasseA, ClasseB e ClasseC.");
    }

    @Test
    public void testVerificarCiclosConstrutores_usandoVRInstance() throws IOException {
        criarArquivoClasse(tempDir, "ClasseA", "public class ClasseA { public ClasseA() { VRInstance.criar(ClasseB.class); } }");
        criarArquivoClasse(tempDir, "ClasseB", "public class ClasseB { public ClasseB() { VRInstance.criar(ClasseA.class); } }");

        checker.verificarCiclosConstrutores(tempDir);
        assertTrue(checker.temCicloDetectado(), "Ciclo deve ser detectado com VRInstance.criar.");
    }

    @Test
    public void testVerificarCiclosConstrutores_somenteInstanciaSimples() throws IOException {
        criarArquivoClasse(tempDir, "ClasseA", "public class ClasseA { public ClasseA() { } }");

        checker.verificarCiclosConstrutores(tempDir);
        assertFalse(checker.temCicloDetectado(), "Nenhum ciclo deve ser detectado em uma classe sem dependências.");
    }

    @Test
    public void testVerificarCiclosConstrutores_multiplasClassesSemCiclo() throws IOException {
        criarArquivoClasse(tempDir, "ClasseA", "public class ClasseA { public ClasseA() { new ClasseB(); } }");
        criarArquivoClasse(tempDir, "ClasseB", "public class ClasseB { public ClasseB() { new ClasseC(); } }");
        criarArquivoClasse(tempDir, "ClasseC", "public class ClasseC { public ClasseC() { } }");

        checker.verificarCiclosConstrutores(tempDir);
        assertFalse(checker.temCicloDetectado(), "Nenhum ciclo deve ser detectado em múltiplas classes sem dependências cíclicas.");
    }

    private void criarArquivoClasse(Path dir, String nomeClasse, String conteudo) throws IOException {
        Path arquivoClasse = dir.resolve(nomeClasse + ".java");
        Files.write(arquivoClasse, conteudo.getBytes());
    }
}

