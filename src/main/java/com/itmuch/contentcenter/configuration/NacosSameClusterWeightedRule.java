package com.itmuch.contentcenter.configuration;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.ribbon.NacosServer;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.client.naming.core.Balancer;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.BaseLoadBalancer;
import com.netflix.loadbalancer.Server;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 同集群优先的支持nacos权重的负载规则
 */
@Slf4j
public class NacosSameClusterWeightedRule extends AbstractLoadBalancerRule {

    @Autowired
    private NacosDiscoveryProperties nacosDiscoveryProperties;

    /**
     * 用于读取配置文件，并初始化NacosWeightedRule
     *
     * 我们不需要，留空
     * @param iClientConfig
     */
    @Override
    public void initWithNiwsConfig(IClientConfig iClientConfig) {

    }

    @Override
    public Server choose(Object o) {
        /**
         * 1、找到指定方服务的所有实例
         * 2、过滤出相同集群下的所有实例
         * 3、基于权重的负载均衡算法，返回一个实例
         */
        try {
            //拿到配置文件中的nacos配置的cluster-name名称
            String clusterName = nacosDiscoveryProperties.getClusterName();

            BaseLoadBalancer loadBalancer = (BaseLoadBalancer) this.getLoadBalancer();
            //想要请求的微服务（服务提供者）的名称
            String name = loadBalancer.getName();

            //拿到服务发现相关的API
            NamingService namingService = nacosDiscoveryProperties.namingServiceInstance();

            //1、找到指定方服务的所有（健康的）实例
            List<Instance> instances = namingService.selectInstances(name, true);

            //2、过滤出相同集群下的所有实例
            List<Instance> sameClusterInstances = instances.stream()
                    .filter(instance -> {
                        return Objects.equals(instance.getClusterName(), clusterName);
                    })
                    .collect(Collectors.toList());

            List<Instance> instancesToBeChose = new ArrayList<>();
            if (CollectionUtils.isEmpty(sameClusterInstances)){
                instancesToBeChose = instances;
                log.info("发生跨集群的调用，name={},cluster_name={},instance={}",name,clusterName,instances);
            }else{
                instancesToBeChose = sameClusterInstances;
            }

            //3、基于权重的负载均衡算法，返回一个实例
            Instance instance = ExtendBalancer.selftGetHostByRandomWeight(instancesToBeChose);
            log.info("选择的实例是Port={},instance={}",instance.getPort(),instance);
            return new NacosServer(instance);
        } catch (NacosException e) {
            e.printStackTrace();
            return null;
        }
    }
}

class ExtendBalancer extends Balancer{
    public static Instance selftGetHostByRandomWeight(List<Instance> hosts) {
        return getHostByRandomWeight(hosts);
    }

}
