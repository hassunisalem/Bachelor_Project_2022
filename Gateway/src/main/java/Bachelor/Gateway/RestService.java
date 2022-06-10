package Bachelor.Gateway;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
public class RestService{

    //private Neo4jDriver neo4jDriver = new Neo4jDriver("neo4j://localhost:7687", "neo4j", "demo");
/*
    @GetMapping("/")
    public ResponseEntity getCount(@PathVariable String word) {
        return new ResponseEntity(HttpStatus.OK);
    }


*/
    private final Neo4jDriver neo4jDriver = new Neo4jDriver("neo4j://localhost:7687", "neo4j", "demo");


    @PostMapping("addWP/")
    public String saveFiles(@RequestBody String files) throws InterruptedException {

        String WPs = files.substring(files.indexOf("---")+3);
        String changes = files.substring(0,files.indexOf("---"));
        neo4jDriver.addWebpartRelation(changes,WPs.toUpperCase());
        //neo4jDriver.createSdkUi(files);
        return "Posted";
    }

    @PostMapping("addBug/")
    public String addBug(@RequestBody String files) throws InterruptedException {

        String WPs = files.substring(files.indexOf("---")+3);
        neo4jDriver.addBugRelation(files,WPs.toUpperCase());
        //neo4jDriver.createSdkUi(files);
        return "Posted";
    }

    @GetMapping("/getRecommendation")
    public ArrayList<String> GetWPRecommendation(@RequestParam(value = "files") String files) throws InterruptedException {
        System.out.println(neo4jDriver.GetWPRecommendation(files));
        return neo4jDriver.GetWPRecommendation(files);
    }


}
