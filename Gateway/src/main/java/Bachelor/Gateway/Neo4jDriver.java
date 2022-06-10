package Bachelor.Gateway;

import org.neo4j.driver.*;
import org.neo4j.driver.net.ServerAddress;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.neo4j.driver.SessionConfig.builder;

public class Neo4jDriver implements AutoCloseable {
    private final Driver driver;

    public Neo4jDriver(String uri, String user, String password) {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    @Override
    public void close() throws Exception {
        driver.close();
    }

    private Driver createDriver( String virtualUri, String user, String password, ServerAddress... addresses )
    {
        Config config = Config.builder()
                .withResolver( address -> new HashSet<>( Arrays.asList( addresses ) ) )
                .build();

        return GraphDatabase.driver( virtualUri, AuthTokens.basic( user, password ), config );
    }
    public void addBugRelation(String Files, String bug) throws InterruptedException {
        String username = "neo4j";
        String password = "demo";
        //   String files = "src/components/accordion/Accordion.css src/components/accordion/Accordion.tsxsrc/components/advancedSettings/AdvancedSettings.csssrc/components/advancedSettings/randomfil/AdvancedSettings.tsx src/components/analytics/Analytics.css src/components/analytics/Analytics.tsx";
        String[] rawFiles = Files.split("src");
        int folderIndex;
        int relationIndex;
        StringBuilder MERGEFolderNode = new StringBuilder("MERGE (f0:Folder {name:\"src\"})");
        StringBuilder MERGEWPNode = new StringBuilder(new String("MERGE (wp0:Bug {name:\"" + bug + "\"})"));

        StringBuilder WITHFolders = new StringBuilder(new String("WITH f0,"));
        StringBuilder WITHWPs = new StringBuilder(new String(",wp0"));

        StringBuilder MERGERelation = new StringBuilder(new String());
        StringBuilder MERGEWPRelation = new StringBuilder(new String("MERGE (wp0)-[:FIXED_ON]->(f0) "));

        StringBuilder onCreateOnSet = new StringBuilder(new String(""));
        String cypherQuery  = "";


        StringBuilder deleteDublicateRs = new StringBuilder(new String("MATCH (a)-[r:Contains]->(b)\n" +
                "WITH a, b, TYPE(r) AS t, COLLECT(r) AS rr\n" +
                "WHERE SIZE(rr) > 1\n" +
                "WITH rr\n" +
                "LIMIT 100000\n" +
                "FOREACH (r IN TAIL(rr) | DELETE r);"));

        String mergeNodeTrim = null;
        String withTrim = null;
        String jsonWrapped = new String();
        String pathId = new String();

        /*
   Merge(f1:Folder {name:"components"})
    merge (f2:Folder{name:"advancedSettings"})
    Merge(f3:Folder {name:"test"})
    merge (f4:File{name:"test2", type:"css"})

 WITH f1,f2,f3,f4
 merge (f1)-[:Contains]->(f2)-[:Contains]->(f3)-[:Contains]->(f4)
 merge (f1)-[a1:Admin]->(f2)-[a2:Admin]->(f3)-[a3:Admin]->(f4)
    on create set a1.Score = 0
    on create set a2.Score = 0
    on create set a3.Score = 0
    on match set a1.Score = a1.Score + 1
    on match set a2.Score = a2.Score + 1
    on match set a3.Score = a3.Score + 1
         */
        String WpRelationName = "";
        // System.out.println(webParts.size());
        relationIndex = 0;
        int layer = 1;
        folderIndex = 1;
        for (String path:rawFiles) {
            String[] line = path.split("/");



            for (String folder: line)
            {
                if (folder.isBlank())
                {
                    continue;
                }
                pathId += folder.substring(0,1).toUpperCase() + folder.substring(folder.length()/2, folder.length()/2 + 1).toLowerCase() + folder.substring(folder.length()-1).toUpperCase();

                if (folderIndex == 0)
                {
                    //    MERGENode.append(" {\n\"folderIndex").append(folderIndex+"\"").append(": ").append("\""+folder+"\"").append(", ");

                }
                /*
                else if (folder == rawFiles[pathIndex-1].split("/")[pathIndex]){
                    System.out.println("true");
                    continue;
                }
                 */
                if (relationIndex == 0)
                {

                    //MERGERelation.append("MERGE (wp0)-[:"+ webParts.get(0)+"]->(f").append(folderIndex ).append(")");

                }
                if (folder.contains("."))
                {
                    String fileType= folder.substring(folder.indexOf("."));
                    //String fileName= folder.substring(0,folder.length()-folder.indexOf("."));
                    // MERGENode.append("\"fileName\"").append(": ").append("\""+folder+"\"").append(", ").append("\"fileType\"").append(": ").append("\""+fileType+"\"").append("},\n");
                    MERGEFolderNode.append("MERGE (f").append(folderIndex).append(":File {name:\"").append(folder).append("\",type:\"").append(fileType).append("\",pathId:\"").append(pathId +"\"})");
                    // MERGEWPNode.append("MERGE (wp").append(folderIndex);
                    WITHFolders.append("f").append(folderIndex +",");
                    // WITHWPs.append("wp").append(folderIndex +",");
                    onCreateOnSet.append("on match set r"+(relationIndex)+".Score = r"+(relationIndex)+".Score + 1\n");
                    onCreateOnSet.append("on create set r"+(relationIndex)+".Score = 0\n");
                    pathId ="";
                    layer = 1;
                 //   MERGERelation.append("" + "\n");
                }
                else{
                    /*
                    if(false){
                    MERGENode.append("CREATE (f").append(folderIndex).append(":Folder {name: \"").append(folder).append("\",layer:\"").append( layer+"\"})");
                    }
                     */
                    MERGEWPRelation.append("MERGE (f" + folderIndex +")-[r"+ folderIndex +":FIXED_ON]->(f" + (folderIndex+1)+") ");
                    MERGEFolderNode.append("MERGE (f").append(folderIndex).append(":Folder {name: \"").append(folder).append("\",layer:").append( layer+",pathId:\"").append(pathId +"\"})");

/*
                if (!webParts.isEmpty()){
                    for (String wp:webParts) {

                    }
                }
 */
                    // MERGENode.append("\"folderIndex").append(folderIndex+"\"").append(": ").append("\""+folder+"\"").append(", ");
                    WITHFolders.append("f").append(folderIndex).append(",");
                    /*
                    if (webParts.size() >= folderIndex){
                        WITHFolders.append("wp").append(folderIndex).append(",");
                    }

                     */

                    onCreateOnSet.append("on match set r"+relationIndex+".Score = r"+relationIndex+".Score + 1\n");
                    onCreateOnSet.append("on create set r"+relationIndex+".Score = 0\n");

                    //  MERGERelation.append("-[:Contains]->(f").append(folderIndex+1).append(")");

                    layer++;
                }
                folderIndex++;
                relationIndex++;
///                jsonWrapped = "[ \n" + mergeNodeTrim.substring(0, mergeNodeTrim.length()-2) + "\n]";

            }

            relationIndex = 0;
            folderIndex = 1;
            // Thread.sleep(60000);

            //System.out.println(withTrim +" " + MERGERelation + " " + onCreateOnSet);
            String MERGEFolderNodes = String.valueOf(MERGEFolderNode);

            cypherQuery = MERGEFolderNodes + MERGEWPNode + "\n" + WITHFolders.substring(0,WITHFolders.length()-1) + WITHWPs +"\n" + MERGEWPRelation +"\n" + onCreateOnSet;
            // MERGEFolderNode.append("\n" + WITHFolders.substring(0,WITHFolders.length()-1) + "\n" + MERGERelation + "\n" + onCreateOnSet );
            mergeNodeTrim = String.valueOf(MERGEFolderNode);
            String deleteTrim = String.valueOf(deleteDublicateRs);
            System.out.printf(cypherQuery);
            MERGEFolderNode = new StringBuilder("MERGE (f0:Folder {name:\"src\"})");
            WITHFolders = new StringBuilder("WITH f0,");
            MERGEWPRelation = new StringBuilder(new String("MERGE (wp0)-[r:FIXED_ON]->(f0) MERGE (f0)-[r0:FIXED_ON]->(f1) "));
            MERGEWPNode = new  StringBuilder(new String("MERGE (wp0:WebPart {name:\"" + bug + "\"})"));

            onCreateOnSet = new StringBuilder(new String(""));

            try ( Driver driver = createDriver( "neo4j://localhost:7687", username, password, ServerAddress.of( "localhost", 7687 ) ) )
            {
                try ( Session session = driver.session( builder().withDefaultAccessMode( AccessMode.WRITE ).build() ) )
                {
                    session.run(cypherQuery);
                    //session.run(deleteTrim);
                }
            }
        }
        /*
                withTrim = String.valueOf(WITHFolders);
        MERGENode = new StringBuilder(new String("MERGE (f0:Folder {name:\"Src\"})"));
        WITHFolders = new StringBuilder(new String("WITH f0,"));
        MERGERelation = new StringBuilder(new String());


         */

    }

    public void addWebpartRelation(String name, String webPart) throws InterruptedException {
        String username = "neo4j";
        String password = "demo";
     //   String files = "src/components/accordion/Accordion.css src/components/accordion/Accordion.tsxsrc/components/advancedSettings/AdvancedSettings.csssrc/components/advancedSettings/randomfil/AdvancedSettings.tsx src/components/analytics/Analytics.css src/components/analytics/Analytics.tsx";
        String[] rawFiles = name.split("src");
        int folderIndex;
        int relationIndex;
        StringBuilder MERGEFolderNode = new StringBuilder("MERGE (f0:Folder {name:\"src\"})");
        StringBuilder MERGEWPNode = new StringBuilder(new String("MERGE (wp0:WebPart {name:\"" + webPart + "\"})"));

        StringBuilder WITHFolders = new StringBuilder(new String("WITH f0,"));
        StringBuilder WITHWPs = new StringBuilder(new String(",wp0"));

        StringBuilder MERGEFolderRelation = new StringBuilder(new String());

        StringBuilder MERGERelation = new StringBuilder(new String());
        StringBuilder MERGEWPRelation = new StringBuilder(new String("MERGE (wp0)-[:Webpart {name: \""+ webPart + "\"}]->(f0) "));



        StringBuilder onCreateOnSet = new StringBuilder(new String(""));
        String cypherQuery  = "";


        StringBuilder deleteDublicateRs = new StringBuilder(new String("MATCH (a)-[r:Contains]->(b)\n" +
                "WITH a, b, TYPE(r) AS t, COLLECT(r) AS rr\n" +
                "WHERE SIZE(rr) > 1\n" +
                "WITH rr\n" +
                "LIMIT 100000\n" +
                "FOREACH (r IN TAIL(rr) | DELETE r);"));

        String mergeNodeTrim = null;
        String withTrim = null;
        String jsonWrapped = new String();
        String pathId = new String();

        /*
   Merge(f1:Folder {name:"components"})
    merge (f2:Folder{name:"advancedSettings"})
    Merge(f3:Folder {name:"test"})
    merge (f4:File{name:"test2", type:"css"})

 WITH f1,f2,f3,f4
 merge (f1)-[:Contains]->(f2)-[:Contains]->(f3)-[:Contains]->(f4)
 merge (f1)-[a1:Admin]->(f2)-[a2:Admin]->(f3)-[a3:Admin]->(f4)
    on create set a1.Score = 0
    on create set a2.Score = 0
    on create set a3.Score = 0
    on match set a1.Score = a1.Score + 1
    on match set a2.Score = a2.Score + 1
    on match set a3.Score = a3.Score + 1
         */
        String WpRelationName = "";
       // System.out.println(webParts.size());
        relationIndex = 0;
        int layer = 1;
            folderIndex = 1;
        for (String path:rawFiles) {
            String[] foldersAndFiles = path.split("/");
            if (path.isBlank())
            {
                continue;
            }
            for (String s: foldersAndFiles)
            {
                String f = s.replaceAll("\\s","");

                if (f.isBlank())
                {
                    continue;
                }
                pathId += f.substring(0,1).toUpperCase() + f.substring(f.length()/2, f.length()/2 + 1).toLowerCase() + f.substring(f.length()-1).toUpperCase();

                if (folderIndex == 0)
                {
                //    MERGENode.append(" {\n\"folderIndex").append(folderIndex+"\"").append(": ").append("\""+folder+"\"").append(", ");

                }
                /*
                else if (folder == rawFiles[pathIndex-1].split("/")[pathIndex]){
                    System.out.println("true");
                    continue;
                }
                 */
                if (relationIndex == 0)
                {
                    MERGEFolderRelation.append("MERGE (f0)-[:Contains]->(f").append(folderIndex).append(")");
                    //MERGERelation.append("MERGE (wp0)-[:"+ webParts.get(0)+"]->(f").append(folderIndex ).append(")");

                }
                if (f.contains("."))
                {
                    String fileType= f.substring(f.indexOf("."));
                    //String fileName= folder.substring(0,folder.length()-folder.indexOf("."));
                   // MERGENode.append("\"fileName\"").append(": ").append("\""+folder+"\"").append(", ").append("\"fileType\"").append(": ").append("\""+fileType+"\"").append("},\n");
                    MERGEFolderNode.append("MERGE (f").append(folderIndex).append(":File {name:\"").append(f).append("\",type:\"").append(fileType).append("\",pathId:\"").append(pathId +"\"})");

                   // MERGEWPNode.append("MERGE (wp").append(folderIndex);
                    WITHFolders.append("f").append(folderIndex +",");
                   // WITHWPs.append("wp").append(folderIndex +",");
                  //  onCreateOnSet.append("on match set r"+(folderIndex)+".Score = r"+(folderIndex)+".Score + 1\n");
                   // onCreateOnSet.append("on create set r"+(folderIndex)+".Score = 0\n");
                    pathId ="";
                    layer = 1;
                  //  MERGERelation.append("" + "\n");
                }
                else{
                    /*
                    if(false){
                    MERGENode.append("CREATE (f").append(folderIndex).append(":Folder {name: \"").append(folder).append("\",layer:\"").append( layer+"\"})");
                    }
                     */
                    MERGEWPRelation.append("MERGE (f" + folderIndex +")-[r"+ folderIndex +":Webpart {name: \""+ webPart + "\"}]->(f" + (folderIndex+1)+") ");
                    MERGEFolderNode.append("MERGE (f").append(folderIndex).append(":Folder {name: \"").append(f).append("\",layer:").append( layer+",pathId:\"").append(pathId +"\"})");
                    MERGEFolderRelation.append("-[:Contains]->(f").append(folderIndex+1).append(")");
/*
                if (!webParts.isEmpty()){
                    for (String wp:webParts) {

                    }
                }
 */
                   // MERGENode.append("\"folderIndex").append(folderIndex+"\"").append(": ").append("\""+folder+"\"").append(", ");
                    WITHFolders.append("f").append(folderIndex).append(",");
                    /*
                    if (webParts.size() >= folderIndex){
                        WITHFolders.append("wp").append(folderIndex).append(",");
                    }

                     */

                    onCreateOnSet.append("on match set r"+folderIndex+".Score = r"+folderIndex+".Score + 1\n");
                    onCreateOnSet.append("on create set r"+folderIndex+".Score = 0\n");

                  //  MERGERelation.append("-[:Contains]->(f").append(folderIndex+1).append(")");

                    layer++;
                }
                folderIndex++;
                relationIndex++;
///                jsonWrapped = "[ \n" + mergeNodeTrim.substring(0, mergeNodeTrim.length()-2) + "\n]";

            }

            relationIndex = 0;
             folderIndex = 1;
                // Thread.sleep(60000);

        //System.out.println(withTrim +" " + MERGERelation + " " + onCreateOnSet);
        String MERGEFolderNodes = String.valueOf(MERGEFolderNode);

        cypherQuery = MERGEFolderNodes + MERGEWPNode + "\n" + WITHFolders.substring(0,WITHFolders.length()-1) + WITHWPs +"\n" + MERGEFolderRelation + "\n" + MERGEWPRelation +"\n" + onCreateOnSet;
        // MERGEFolderNode.append("\n" + WITHFolders.substring(0,WITHFolders.length()-1) + "\n" + MERGERelation + "\n" + onCreateOnSet );
        mergeNodeTrim = String.valueOf(MERGEFolderNode);
        String deleteTrim = String.valueOf(deleteDublicateRs);
             System.out.printf(cypherQuery);
             MERGEFolderRelation = new StringBuilder(new String());
            MERGEFolderNode = new StringBuilder("MERGE (f0:Folder {name:\"src\"})");
            WITHFolders = new StringBuilder("WITH f0,");
            MERGEWPRelation = new StringBuilder(new String("MERGE (wp0)-[r:Webpart {name:\"" + webPart + "\"}]->(f0) MERGE (f0)-[r0:Webpart {name: \""+ webPart + "\"}]->(f1) "));
            MERGEWPNode = new  StringBuilder(new String("MERGE (wp0:WebPart {name:\"" + webPart + "\"})"));

            onCreateOnSet = new StringBuilder(new String(""));

        try ( Driver driver = createDriver( "neo4j://localhost:7687", username, password, ServerAddress.of( "localhost", 7687 ) ) )
        {
            try ( Session session = driver.session( builder().withDefaultAccessMode( AccessMode.WRITE ).build() ) )
            {
                session.run(cypherQuery);
                //session.run(deleteTrim);
            }
        }
        }
        /*
                withTrim = String.valueOf(WITHFolders);
        MERGENode = new StringBuilder(new String("MERGE (f0:Folder {name:\"Src\"})"));
        WITHFolders = new StringBuilder(new String("WITH f0,"));
        MERGERelation = new StringBuilder(new String());


         */

    }
    public void createSdkUi(String name) throws InterruptedException {
        String username = "neo4j";
        String password = "demo";
        String files = "src/components/accordion/Accordion.css src/components/accordion/Accordion.tsxsrc/components/advancedSettings/AdvancedSettings.csssrc/components/advancedSettings/randomfil/AdvancedSettings.tsx src/components/analytics/Analytics.css src/components/analytics/Analytics.tsx";
        String[] rawFiles = name.split("src");
        int folderIndex;
        int relationIndex;
        StringBuilder MERGENode = new StringBuilder(new String("MERGE (f0:Folder {name:\"src\", layer: 0})"));
        StringBuilder WITH = new StringBuilder(new String("WITH f0,"));
        StringBuilder MERGEFolderRelation = new StringBuilder(new String());
        StringBuilder MERGEWPRelation = new StringBuilder(new String());

        StringBuilder deleteDublicateRs = new StringBuilder(new String("MATCH (a)-[r:Contains]->(b)\n" +
                "WITH a, b, TYPE(r) AS t, COLLECT(r) AS rr\n" +
                "WHERE SIZE(rr) > 1\n" +
                "WITH rr\n" +
                "LIMIT 100000\n" +
                "FOREACH (r IN TAIL(rr) | DELETE r);"));
        String deleteTrim = String.valueOf(deleteDublicateRs);
        String mergeNodeTrim = null;
        String withTrim = null;
        String jsonWrapped = new String();
        String pathId = new String();

        /*
MERGE (wp0:WebPart{name:"Admin"}) MERGE (f0:Folder {name:"Src"})MERGE (f1:Folder {name: "components",layer:"1",pathId:"CnS"})MERGE (f2:Folder {name: "analytics",layer:"2",pathId:"CnSAyS"})MERGE (f3:Folder {name: "tabs",layer:"3",pathId:"CnSAySTbS"})Merge (f4:File {name:"MessagesTab.tsx
",type:".tsx
",pathId:"CnSAySTbSMt
"})

WITH wp0,f0,f1,f2,f3,f4
MERGE (f0)-[:Contains]->(f1)-[:Contains]->(f2)-[:Contains]->(f3)-[:Contains]->(f4)
MERGE (wp0)-[wp0s:ADMIN]->(f0)
on create set wp0s.Score = 0
on match set wp0s.Score = wp0s.Score + 1
MERGE (f0)-[wp1s:ADMIN]->(f1)
on create set wp1s.Score = 0
on match set wp1s.Score = wp1s.Score + 1
MERGE (f1)-[wp2s:ADMIN]->(f2)
on create set wp2s.Score = 0
on match set wp2s.Score = wp2s.Score + 1
MERGE (f2)-[wp3s:ADMIN]->(f3)
on create set wp3s.Score = 0
on match set wp3s.Score = wp3s.Score + 1
MERGE (f3)-[wp4s:ADMIN]->(f4)
on create set wp4s.Score = 0
on match set wp4s.Score = wp4s.Score + 1

         */
        int layer = 1;
        int bacthSize = 0;
        folderIndex = 1;
        for (String path:rawFiles) {
            String[] line = path.split("/");
            relationIndex = 0;
            if (path.isBlank())
            {
                continue;
            }
            for (String s: line)
            {
                String f = s.replaceAll("\\s","");
                if (f.isBlank())
                {
                    continue;
                }
                pathId += f.substring(0,1).toUpperCase() + f.substring(f.length()/2, f.length()/2 + 1).toLowerCase() + f.substring(f.length()-1).toUpperCase();

                if (folderIndex == 0)
                {
                    //    MERGENode.append(" {\n\"folderIndex").append(folderIndex+"\"").append(": ").append("\""+folder+"\"").append(", ");

                }
                /*
                else if (folder == rawFiles[pathIndex-1].split("/")[pathIndex]){
                    System.out.println("true");
                    continue;
                }
                 */
                if (relationIndex == 0)
                {

                    MERGEFolderRelation.append("MERGE (f0)-[:Contains]->(f").append(folderIndex).append(")");

                }
                if (f.contains("."))
                {
                    String fileType= f.substring(f.indexOf("."));
                    //String fileName= folder.substring(0,folder.length()-folder.indexOf("."));
                    // MERGENode.append("\"fileName\"").append(": ").append("\""+folder+"\"").append(", ").append("\"fileType\"").append(": ").append("\""+fileType+"\"").append("},\n");
                    MERGENode.append("Merge (f").append(folderIndex).append(":File {name:\"").append(f).append("\",type:\"").append(fileType).append("\",layer:").append(layer +",pathId:\"").append(pathId +"\"})");
                    WITH.append("f").append(folderIndex).append(",");

                    pathId ="";
                    layer = 1;
                    //MERGEFolderRelation.append("" + "\n");
                }
                else{
                    relationIndex++;
                    /*
                    if(false){
                    MERGENode.append("CREATE (f").append(folderIndex).append(":Folder {name: \"").append(folder).append("\",layer:\"").append( layer+"\"})");
                    }

                     */

                    MERGENode.append("MERGE (f").append(folderIndex).append(":Folder {name: \"").append(f).append("\",layer:").append( layer+",pathId:\"").append(pathId +"\"})");
                    /*
                    if (!webParts.isEmpty()){
                        for (String wp:webParts) {

                        }
                    }

                     */
                    // MERGENode.append("\"folderIndex").append(folderIndex+"\"").append(": ").append("\""+folder+"\"").append(", ");
                    WITH.append("f").append(folderIndex).append(",");

                    MERGEFolderRelation.append("-[:Contains]->(f").append(folderIndex+1).append(")");

                    layer++;
                }
                folderIndex++;

///                jsonWrapped = "[ \n" + mergeNodeTrim.substring(0, mergeNodeTrim.length()-2) + "\n]";

            }
            bacthSize++;


                MERGENode.append("\n" + WITH.substring(0,WITH.length()-1) + "\n" + MERGEFolderRelation );
                mergeNodeTrim = String.valueOf(MERGENode);

                System.out.printf(mergeNodeTrim);

                try ( Driver driver = createDriver( "neo4j://localhost:7687", username, password, ServerAddress.of( "localhost", 7687 ) ) ) {
                    try (Session session = driver.session(builder().withDefaultAccessMode(AccessMode.WRITE).build())) {
                        session.run(mergeNodeTrim);
                        session.run(deleteTrim);
                    }
                }



                MERGENode = new StringBuilder(new String("MERGE (f0:Folder {name:\"src\", layer:0})"));
                WITH = new StringBuilder(new String("WITH f0,"));
                MERGEFolderRelation = new StringBuilder(new String());

                folderIndex = 1;
                // Thread.sleep(60000);
            bacthSize =0;

        }

    }

    public ArrayList<String> GetWPRecommendation(String name) throws InterruptedException {
        ArrayList<String> recommendations = new ArrayList<>();
        String username = "neo4j";
        String password = "demo";
        String files = "src/components/accordion/Accordion.css src/components/accordion/Accordion.tsxsrc/components/advancedSettings/AdvancedSettings.csssrc/components/advancedSettings/randomfil/AdvancedSettings.tsx src/components/analytics/Analytics.css src/components/analytics/Analytics.tsx";
        String[] rawFiles = name.split("src");
        int folderIndex;
        int relationIndex;
        StringBuilder match = new StringBuilder(new String(""));
        StringBuilder MERGENode = new StringBuilder(new String("MERGE (f0:Folder {name:\"src\", layer: 0})"));
        StringBuilder WITH = new StringBuilder(new String("WITH f0,"));
        StringBuilder MERGEFolderRelation = new StringBuilder(new String());
        StringBuilder MERGEWPRelation = new StringBuilder(new String());

        StringBuilder deleteDublicateRs = new StringBuilder(new String("MATCH (a)-[r:Contains]->(b)\n" +
                "WITH a, b, TYPE(r) AS t, COLLECT(r) AS rr\n" +
                "WHERE SIZE(rr) > 1\n" +
                "WITH rr\n" +
                "LIMIT 100000\n" +
                "FOREACH (r IN TAIL(rr) | DELETE r);"));
        String deleteTrim = String.valueOf(deleteDublicateRs);
        String mergeNodeTrim = null;
        String withTrim = null;
        String jsonWrapped = new String();
        String pathId = new String();
        System.out.println(name);

        /*
MERGE (wp0:WebPart{name:"Admin"}) MERGE (f0:Folder {name:"Src"})MERGE (f1:Folder {name: "components",layer:"1",pathId:"CnS"})MERGE (f2:Folder {name: "analytics",layer:"2",pathId:"CnSAyS"})MERGE (f3:Folder {name: "tabs",layer:"3",pathId:"CnSAySTbS"})Merge (f4:File {name:"MessagesTab.tsx
",type:".tsx
",pathId:"CnSAySTbSMt
"})

WITH wp0,f0,f1,f2,f3,f4
MERGE (f0)-[:Contains]->(f1)-[:Contains]->(f2)-[:Contains]->(f3)-[:Contains]->(f4)
MERGE (wp0)-[wp0s:ADMIN]->(f0)
on create set wp0s.Score = 0
on match set wp0s.Score = wp0s.Score + 1
MERGE (f0)-[wp1s:ADMIN]->(f1)
on create set wp1s.Score = 0
on match set wp1s.Score = wp1s.Score + 1
MERGE (f1)-[wp2s:ADMIN]->(f2)
on create set wp2s.Score = 0
on match set wp2s.Score = wp2s.Score + 1
MERGE (f2)-[wp3s:ADMIN]->(f3)
on create set wp3s.Score = 0
on match set wp3s.Score = wp3s.Score + 1
MERGE (f3)-[wp4s:ADMIN]->(f4)
on create set wp4s.Score = 0
on match set wp4s.Score = wp4s.Score + 1

         */
        int layer = 1;
        int WPR = 0;
        folderIndex = 1;
        String recommendation = null;
        for (String path : rawFiles) {
            String[] line = path.split("/");
            relationIndex = 0;
            if (path.isBlank()) {
                continue;
            }
            for (String s : line) {
                String f = s.replaceAll("\\s", "");
                if (f.isBlank()) {
                    continue;
                }
                pathId += f.substring(0, 1).toUpperCase() + f.substring(f.length() / 2, f.length() / 2 + 1).toLowerCase() + f.substring(f.length() - 1).toUpperCase();

                if (folderIndex == 0) {
                    //    MERGENode.append(" {\n\"folderIndex").append(folderIndex+"\"").append(": ").append("\""+folder+"\"").append(", ");

                }
                /*
                else if (folder == rawFiles[pathIndex-1].split("/")[pathIndex]){
                    System.out.println("true");
                    continue;
                }
                 */
                if (relationIndex == 0) {

                    MERGEFolderRelation.append("MERGE (f0)-[:Contains]->(f").append(folderIndex).append(")");

                }
                if (f.contains(".")) {
                    String fileType = f.substring(f.indexOf("."));
                    //String fileName= folder.substring(0,folder.length()-folder.indexOf("."));
                    // MERGENode.append("\"fileName\"").append(": ").append("\""+folder+"\"").append(", ").append("\"fileType\"").append(": ").append("\""+fileType+"\"").append("},\n");
                    MERGENode.append("Merge (f").append(folderIndex).append(":File {name:\"").append(f).append("\",type:\"").append(fileType).append("\",layer:").append(layer + ",pathId:\"").append(pathId + "\"})");
                    WITH.append("f").append(folderIndex).append(",");

                    match.append("MATCH ()-[r:Webpart]->(:File{layer:" + layer + ", name: \"" + f + "\", pathId: \"" + pathId + "\",type: \"" + fileType + "\"}) \n");
                    match.append("where r.Score > 1\n");
                    match.append("return r.name");
                    pathId = "";
                    layer = 1;
                    //MERGEFolderRelation.append("" + "\n");
                } else {
                    relationIndex++;
                    /*
                    if(false){
                    MERGENode.append("CREATE (f").append(folderIndex).append(":Folder {name: \"").append(folder).append("\",layer:\"").append( layer+"\"})");
                    }

                     */

                    MERGENode.append("MERGE (f").append(folderIndex).append(":Folder {name: \"").append(f).append("\",layer:").append(layer + ",pathId:\"").append(pathId + "\"})");
                    /*
                    if (!webParts.isEmpty()){
                        for (String wp:webParts) {

                        }
                    }

                     */
                    // MERGENode.append("\"folderIndex").append(folderIndex+"\"").append(": ").append("\""+folder+"\"").append(", ");
                    WITH.append("f").append(folderIndex).append(",");

                    MERGEFolderRelation.append("-[:Contains]->(f").append(folderIndex + 1).append(")");

                    layer++;
                }
                folderIndex++;

///                jsonWrapped = "[ \n" + mergeNodeTrim.substring(0, mergeNodeTrim.length()-2) + "\n]";
                System.out.println(f);
            }



            MERGENode.append("\n" + WITH.substring(0, WITH.length() - 1) + "\n" + MERGEFolderRelation);
            mergeNodeTrim = String.valueOf(MERGENode);

            System.out.printf(String.valueOf(match));

            try (Driver driver = createDriver("neo4j://localhost:7687", username, password, ServerAddress.of("localhost", 7687))) {
                try (Session session = driver.session(builder().withDefaultAccessMode(AccessMode.WRITE).build())) {
                    List<Record> recommendationsR = session.run(String.valueOf(match)).list();
                    for (Record r:recommendationsR) {
                        recommendations.add(r.values().toString());
                    }
                }
            }

           // System.out.println(match);
            MERGENode = new StringBuilder(("MERGE (f0:Folder {name:\"src\", layer:0})"));
            match = new StringBuilder(new String());
            WITH = new StringBuilder(("WITH f0,"));
            MERGEFolderRelation = new StringBuilder(new String());

            folderIndex = 1;
            // Thread.sleep(60000);
            WPR++;


        }
        return recommendations;

    }
}
