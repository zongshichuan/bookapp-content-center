package ribbonconfiguration;

import com.itmuch.contentcenter.configuration.NacosSameClusterWeightedRule;
import com.itmuch.contentcenter.configuration.NacosWeightedRule;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.RandomRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RibbonConfiguration {

//    @Bean
//    public IRule ribbonRule(){
//        //随机负载策略
//        return new RandomRule();
//    }

//    @Bean
//    public IRule ribbonRule(){
//        //自定义支持nacos权重的负载策略
//        return new NacosWeightedRule();
//    }

    @Bean
    public IRule ribbonRule(){
        //自定义支持同集群优先、nacos权重的负载策略
        return new NacosSameClusterWeightedRule();
    }

}
