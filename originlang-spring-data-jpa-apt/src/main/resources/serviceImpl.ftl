package ${packageName};

import javax.persistence.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
* ${entityName}
*/
@Service
@Sl4j


public class ${entityName}Service {

private final ${entityName}Repository repository;

@Override
public ${entityName} insert(  ${entityName} entity){
repository.save(entity);
}

}
