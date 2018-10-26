/* eslint-disable */
const HtmlWebpackPlugin = require('html-webpack-plugin')

module.exports = {
  entry: './src/index.js',
  output: {
    path: __dirname + '/build/npm-build/',
    filename: 'bundle.js'
  },
  
  devServer: {
    contentBase: "./src",
    hot: true, 
    https: false,
    proxy: {
      "/openbis": {
        "target": 'https://localhost:8122',
        "changeOrigin": true,
        "secure": false
      }
    }    
  },

  devtool: "source-map",

  mode: 'development',

  module: {
    rules: [
      {
        test: /\.(js|jsx)$/,
        exclude: /node_modules/,
        use: {
          loader: "babel-loader"
        }
      },
      {
        test: /\.(css)$/,
        use: [
          'style-loader',
          'css-loader'
        ]
      },
      {
        test: /\.(woff2?|eot|ttf|otf)(\?.*)?$/,
        loader: 'url-loader',
        options: {
          limit: 10000
        }
      }
    ]
  },

  plugins: [
    new HtmlWebpackPlugin({
      inject: 'body',
      filename: './index.html',
      template: './index.html'
    })
  ]
};
