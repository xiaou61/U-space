#!/bin/bash

echo "🚀 开始构建 U-Space 3.X 微服务项目..."

# 设置颜色
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}📦 步骤 1: 清理和构建Maven项目${NC}"
mvn clean package -DskipTests
if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Maven构建失败！${NC}"
    exit 1
fi

echo -e "${BLUE}📦 步骤 2: 构建Docker镜像${NC}"

# 构建网关镜像
echo -e "${YELLOW}构建网关服务镜像...${NC}"
cd u-space-3.X-gateway
docker build -t u-space/u-space-gateway:1.0-SNAPSHOT .
if [ $? -ne 0 ]; then
    echo -e "${RED}❌ 网关镜像构建失败！${NC}"
    exit 1
fi
cd ..

# 构建用户服务镜像
echo -e "${YELLOW}构建用户服务镜像...${NC}"
cd u-space-3.X-app
docker build -t u-space/u-space-user-service:1.0-SNAPSHOT .
if [ $? -ne 0 ]; then
    echo -e "${RED}❌ 用户服务镜像构建失败！${NC}"
    exit 1
fi
cd ..

# 构建订单服务镜像
echo -e "${YELLOW}构建订单服务镜像...${NC}"
cd u-space-3.X-order-service
docker build -t u-space/u-space-order-service:1.0-SNAPSHOT .
if [ $? -ne 0 ]; then
    echo -e "${RED}❌ 订单服务镜像构建失败！${NC}"
    exit 1
fi
cd ..

echo -e "${GREEN}✅ 所有镜像构建完成！${NC}"

echo -e "${BLUE}📋 构建的镜像列表：${NC}"
docker images | grep u-space

echo -e "${GREEN}🎉 构建完成！现在可以使用以下命令启动服务：${NC}"
echo -e "${YELLOW}cd docs/dev-ops${NC}"
echo -e "${YELLOW}docker-compose -f docker-compose-microservices.yml up -d${NC}"

echo -e "${BLUE}📊 服务地址：${NC}"
echo -e "${GREEN}- API网关: http://localhost:8080${NC}"
echo -e "${GREEN}- 用户服务: http://localhost:8081${NC}"
echo -e "${GREEN}- 订单服务: http://localhost:8082${NC}"
echo -e "${GREEN}- Nacos控制台: http://localhost:8848/nacos (nacos/nacos)${NC}" 