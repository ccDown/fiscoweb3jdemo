package com.kuan.fiscowebdemo;

import org.apache.commons.io.FileUtils;
import org.fisco.bcos.web3j.codegen.SolidityFunctionWrapperGenerator;
import org.fisco.bcos.web3j.solidity.compiler.CompilationResult;
import org.fisco.bcos.web3j.solidity.compiler.SolidityCompiler;
import org.fisco.bcos.web3j.tx.txdecode.CompileSolidityException;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.fisco.bcos.web3j.solidity.compiler.SolidityCompiler.Options.ABI;
import static org.fisco.bcos.web3j.solidity.compiler.SolidityCompiler.Options.BIN;
import static org.fisco.bcos.web3j.solidity.compiler.SolidityCompiler.Options.INTERFACE;
import static org.fisco.bcos.web3j.solidity.compiler.SolidityCompiler.Options.METADATA;

/**
 * @author soullistener
 * Created on 2019/12/19.
 * @description
 */
public class SolToJava {

    public static final String SOLIDITY_PATH = "contracts/solidity/";
    public static final String JAVA_PATH = "contracts/sdk/java/";
    public static final String ABI_PATH = "contracts/sdk/abi/";
    public static final String BIN_PATH = "contracts/sdk/bin/";

    public static final String CLASS_PATH = JAVA_PATH+"classess/";

    public static void main(String[] args) {

        String solName = "HelloWorld.sol";

        File solFileList = new File(SOLIDITY_PATH);
        String tempDirPath = new File(JAVA_PATH).getAbsolutePath();
        try {
            compileSolToJava(solName, tempDirPath, CLASS_PATH, solFileList, ABI_PATH, BIN_PATH);
            System.out.println(
                    "\nCompile solidity contract files to java contract files successfully!");
        } catch (IOException e) {
            System.out.print("error:"+e.getMessage());
        }
    }

    public static void compileSolToJava(
            String solName,
            String tempDirPath,
            String packageName,
            File solFileList,
            String abiDir,
            String binDir)
            throws IOException {
        File[] solFiles = solFileList.listFiles();
        if (solFiles.length == 0) {
            System.out.println("The contracts directory is empty.");
            return;
        }
        for (File solFile : solFiles) {
            if (!solFile.getName().endsWith(".sol")) {
                continue;
            }
            if (!"*".equals(solName)) {
                if (!solFile.getName().equals(solName)) {
                    continue;
                }
                if (solFile.getName().startsWith("Lib")) {
                    throw new IOException("Don't deploy the library: " + solFile.getName());
                }
            } else {
                if (solFile.getName().startsWith("Lib")) {
                    continue;
                }
            }
            SolidityCompiler.Result res =
                    SolidityCompiler.compile(solFile, true, ABI, BIN, INTERFACE, METADATA);
            if ("".equals(res.output)) {
                throw new CompileSolidityException("Compile error: " + res.errors);
            }
            CompilationResult result = CompilationResult.parse(res.output);
            String contractname = solFile.getName().split("\\.")[0];
            CompilationResult.ContractMetadata a =
                    result.getContract(solFile.getName().split("\\.")[0]);
            FileUtils.writeStringToFile(new File(abiDir + contractname + ".abi"), a.abi);
            FileUtils.writeStringToFile(new File(binDir + contractname + ".bin"), a.bin);
            String binFile;
            String abiFile;
            String filename = contractname;
            abiFile = abiDir + filename + ".abi";
            binFile = binDir + filename + ".bin";
            SolidityFunctionWrapperGenerator.main(
                    Arrays.asList(
                            "-a", abiFile,
                            "-b", binFile,
                            "-p", packageName,
                            "-o", tempDirPath)
                            .toArray(new String[0]));
        }
    }


}
