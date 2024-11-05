package constructorcyclechecker;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    
    public static void main(String[] args) throws IOException {
        Path diretorio = Paths.get("C:\\git\\main\\VRPdv\\src\\main\\java\\vrpdv");
        new ConstructorCycleChecker().verificarCiclosConstrutores(diretorio);
    }
    
}