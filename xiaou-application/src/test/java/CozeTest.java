import com.xiaou.CodeNestApplication;
import com.xiaou.common.core.domain.Result;
import com.xiaou.common.enums.CozeWorkflowEnum;
import com.xiaou.common.utils.CozeUtils;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
@SpringBootTest(classes = CodeNestApplication.class)
public class CozeTest {
    @Resource
    private CozeUtils cozeUtils;
    @Test
    public void testCoze() {
        // 运行工作流
        Result<String> result = cozeUtils.runWorkflow(CozeWorkflowEnum.TEST, null);
        System.out.println(result);
    }
}
