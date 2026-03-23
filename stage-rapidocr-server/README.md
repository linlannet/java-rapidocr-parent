# java-rapidocr-parent

[![License](https://img.shields.io/badge/license-Apache%202-green.svg)](https://www.apache.org/licenses/LICENSE-2.0)
![java version](https://img.shields.io/badge/JAVA-1.8-green.svg)
![gitee star](https://gitee.com/linlannet/java-rapidocr-parent/badge/star.svg)

## 介绍
基于RapidOcr的java引擎，可通过此工程实现图片ocr识别，并将基于选择模板数据封装为API接口提供给外部应用访问，底层工具包基于

## 版本历史

```
1.0.3   2025-06-25      rapidocr资源增加两个包：ncnn的linux-arm64和onnx的linux-arm64
linux-arm64的ncnn版本为1.2.0
linux-arm64的onnx版本为1.2.3
profile内增加linux-arm64配置，方便打包输出
stage工程单独上传服务路径配置

1.0.2   2025-06-17      rapidocr资源调整为1.0.0版本
为方便资源上传repo，groupId修改为：net.linlan.tools
stage-rapidoc-server修改为stage-rapidocr-server
打包linux可运行版本：mvn clean package -P linux-x86_64 -Dlinux-build
打包windows可运行版本：mvn clean install

1.0.1   2025-03-20      新增 stage-rapidoc-server 工程

1.0.0   2025-03-20      基于gitee的开源项目进行hutool版本调整，保留原有资源
```

## 技术路径
### 软件架构
```
Springboot
```
### 安装教程
1. mvn clean install
2. mvn clean deploy

### 使用说明
1. 通过依赖使用
```
   <dependency>
      <groupId>${groupId}</groupId>
      <artifactId>${artifactId}</artifactId>
      <version>${project.version}</version>
      <scope>test</scope>
   </dependency>
```
2. 直接拷贝打包后的jar包

## 参与贡献
1.  Fork 本仓库
2.  从 develop 分支拉取新功能分支（如 `feat/xxx`），**禁止直接在 master/main/develop 分支操作**
3.  提交代码到功能分支
4.  新建 Pull Request 到 develop 分支，等待 review
5.  develop 分支 review 通过后，再合并到 master/main
6.  创建和提交 tag（如需发版）
```
git config user.name linlaninfo
git config user.email linlannet@163.com
git config --global --list
git config --list
```
7. 本地编译：`mvn clean install`
8. 发版发布：`mvn clean deploy`

> **分支规范**：`master/main` = 稳定发版分支，`develop` = 开发主分支，功能分支从 develop 拉取。
> **重要**：必须 `mvn compile` 编译通过后才能 commit 和 push，禁止提交无法编译的代码。
