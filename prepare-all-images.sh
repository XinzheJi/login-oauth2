#!/usr/bin/env bash

set -e

# 说明：
# 在「可出网」的本地环境（例如带 OrbStack / Docker Desktop 的电脑）执行本脚本，
# 会完成三件事：
# 1）拉取本项目所需的基础镜像；
# 2）使用项目内的 Dockerfile 构建业务镜像（后端 + tk 大屏前端）；
# 3）将所有基础镜像和业务镜像打包为 tar 文件，放到项目根目录的 docker-images/ 目录，
#    然后你把这些 tar 发给运维，在内网平台导入即可。

ARCH="${ARCH:-linux/amd64}"
OUTPUT_DIR="docker-images"
mkdir -p "${OUTPUT_DIR}"

echo "输出目录：${OUTPUT_DIR}，目标平台：${ARCH}"

########################################
# 一、准备基础镜像（仅需在可出网环境执行）
########################################

BASE_IMAGES=(
  "maven:3.9-eclipse-temurin-17"
  "eclipse-temurin:17-jre"
  "node:18-alpine"
  "nginx:1.25-alpine"
)

echo "=== 第一步：拉取并打包基础镜像 ==="
for IMAGE in "${BASE_IMAGES[@]}"; do
  SAFE_NAME=$(echo "${IMAGE}" | tr '/:' '_')
  TAR_PATH="${OUTPUT_DIR}/${SAFE_NAME}.tar"

  echo "[Base] 处理镜像: ${IMAGE}"
  docker pull --platform="${ARCH}" "${IMAGE}"
  echo "[Base] 已拉取镜像信息："
  docker image inspect "${IMAGE}" --format '   -> OS/Arch: {{.Os}}/{{.Architecture}}'
  docker save "${IMAGE}" -o "${TAR_PATH}"
  echo "[Base] 完成: ${IMAGE} -> ${TAR_PATH}"
  echo
done

########################################
# 二、构建业务镜像（后端 + 管理前端 + tk 大屏 + mock-system-a）
########################################

# 可以通过环境变量覆盖默认 tag，例如：
#   BACKEND_IMAGE_TAG=my-backend:1.0.0
#   WEB_IMAGE_TAG=my-web:1.0.0
#   SCREEN_IMAGE_TAG=my-screen:1.0.0
#   MOCK_IMAGE_TAG=my-mock:1.0.0
BACKEND_IMAGE_TAG="${BACKEND_IMAGE_TAG:-system-b-backend:latest}"
WEB_IMAGE_TAG="${WEB_IMAGE_TAG:-system-b-web-frontend:latest}"
SCREEN_IMAGE_TAG="${SCREEN_IMAGE_TAG:-system-b-screen-frontend:latest}"
MOCK_IMAGE_TAG="${MOCK_IMAGE_TAG:-mock-system-a:latest}"

echo "=== 第二步：构建业务镜像 ==="
echo "[App] 后端镜像: ${BACKEND_IMAGE_TAG}"
docker build --platform="${ARCH}" -f Dockerfile -t "${BACKEND_IMAGE_TAG}" .

echo "[App] 管理前端镜像: ${WEB_IMAGE_TAG}"
(cd vue && docker build --platform="${ARCH}" -t "${WEB_IMAGE_TAG}" .)

echo "[App] tk 大屏镜像: ${SCREEN_IMAGE_TAG}"
(cd tk && docker build --platform="${ARCH}" -t "${SCREEN_IMAGE_TAG}" .)

echo "[App] 模拟鉴权中心镜像: ${MOCK_IMAGE_TAG}"
(cd mock-system-a && docker build --platform="${ARCH}" -t "${MOCK_IMAGE_TAG}" .)

########################################
# 三、打包业务镜像为 tar 文件
########################################

echo "=== 第三步：打包业务镜像 ==="
APP_IMAGES=("${BACKEND_IMAGE_TAG}" "${WEB_IMAGE_TAG}" "${SCREEN_IMAGE_TAG}" "${MOCK_IMAGE_TAG}")

for IMAGE in "${APP_IMAGES[@]}"; do
  SAFE_NAME=$(echo "${IMAGE}" | tr '/:' '_')
  TAR_PATH="${OUTPUT_DIR}/${SAFE_NAME}.tar"

  echo "[App] 打包镜像: ${IMAGE}"
  docker save "${IMAGE}" -o "${TAR_PATH}"
  echo "[App] 完成: ${IMAGE} -> ${TAR_PATH}"
  echo
done

echo "=== 全部完成 ==="
echo "所有基础镜像和业务镜像已保存到 ${OUTPUT_DIR}/ 目录。"
echo "在内网 Docker 环境中，运维可以执行："
echo "  docker load -i <tar文件>"
echo "导入后，就可以在可视化平台中选择这些镜像进行部署。"
echo
echo "如果 tar 上传并导入完成后不再需要本地文件，可以在项目根目录删除 ${OUTPUT_DIR}/。"
