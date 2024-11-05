package constructorcyclechecker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConstructorCycleChecker {
    
    private static final Pattern CLASS_PATTERN = Pattern.compile("class\\s+(\\w+)");
    private static final Pattern CONSTRUCTOR_PATTERN = Pattern.compile("(\\w+)\\s*\\(");
    private static final Pattern CALL_PATTERN = Pattern.compile("(new\\s+(\\w+)\\(|VRInstance\\.criar\\((\\w+)\\.class\\))");

    private Set<String> ciclosDetectados = new HashSet<>();

    public void verificarCiclosConstrutores(Path diretorio) throws IOException {
        Map<String, Set<String>> grafoConstrutores = new HashMap<>();

        Files.walk(diretorio)
                .filter(path -> path.toString().endsWith(".java"))
                .forEach(path -> processarArquivo(path, grafoConstrutores));

        for (String classe : grafoConstrutores.keySet()) {
            if (temCicloConstrutor(classe, grafoConstrutores, new HashSet<>())) {
                System.out.println("Ciclo de construtor detectado na classe: " + classe);
                listarChamadasCiclicas(classe, grafoConstrutores, new HashSet<>());
            }
        }
    }

    private void processarArquivo(Path arquivo, Map<String, Set<String>> grafoConstrutores) {
        try {
            List<String> linhas = Files.readAllLines(arquivo);
            String classeAtual = null;
            Set<String> dependencias = new HashSet<>();

            for (String linha : linhas) {
                Matcher classeMatcher = CLASS_PATTERN.matcher(linha);
                if (classeMatcher.find()) {
                    classeAtual = classeMatcher.group(1);
                }

                if (classeAtual != null) {
                    Matcher construtorMatcher = CONSTRUCTOR_PATTERN.matcher(linha);
                    if (construtorMatcher.find() && construtorMatcher.group(1).equals(classeAtual)) {
                        Matcher callMatcher = CALL_PATTERN.matcher(linha);
                        while (callMatcher.find()) {
                            String chamada = callMatcher.group(2) != null ? callMatcher.group(2) : callMatcher.group(1);
                            String chamadaClasse = chamada.replaceAll("new\\s+|\\(|\\)", ""); // Limpa a string
                            if (chamadaClasse != null) dependencias.add(chamadaClasse);
                        }
                    }
                }
            }

            if (classeAtual != null) {
                grafoConstrutores.putIfAbsent(classeAtual, new HashSet<>());
                grafoConstrutores.get(classeAtual).addAll(dependencias);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean temCicloConstrutor(String classe, Map<String, Set<String>> grafo, Set<String> visitados) {
        if (visitados.contains(classe)) {
            return true;
        }

        visitados.add(classe);
        for (String dependencia : grafo.getOrDefault(classe, Collections.emptySet())) {
            if (temCicloConstrutor(dependencia, grafo, visitados)) {
                return true;
            }
        }
        visitados.remove(classe);
        return false;
    }

    private void listarChamadasCiclicas(String classe, Map<String, Set<String>> grafo, Set<String> visitados) {
        if (visitados.contains(classe)) {
            System.out.println(" -> Retorno ao " + classe);
            return;
        }

        visitados.add(classe);
        System.out.println(classe);

        for (String dependencia : grafo.getOrDefault(classe, Collections.emptySet())) {
            if (!visitados.contains(dependencia)) {
                System.out.println(" -> " + dependencia);
                listarChamadasCiclicas(dependencia, grafo, visitados);
            }
        }
        ciclosDetectados.add(classe);
    }

    public boolean temCicloDetectado() {
        return !ciclosDetectados.isEmpty();
    }
}
