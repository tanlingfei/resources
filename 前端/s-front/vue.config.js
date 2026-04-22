const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    port: 8899,
    client: {
      overlay: false // 禁用浏览器错误覆盖层
    }
  }
})


