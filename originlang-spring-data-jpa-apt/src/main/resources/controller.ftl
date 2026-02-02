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
* ${className}
*/

@RestController
@Slf4j
@RequestMapping("${baseUrl}")
@RequiredArgsConstructor
public class ${className}Controller {


    @PostMapping
    public ${className} insert(@RequestBody  ${className} entity){


    }

}
