# Required software for the build
# brew install git
# brew install npm
# brew install yarn

# Example Build Environment
# git clone https://github.com/ckeditor/ckeditor5-build-inline.git
# cd ckeditor5-build-inline

# Build commands

npm install --save "@ckeditor/ckeditor5-adapter-ckfinder@^18.0.0" \
    "@ckeditor/ckeditor5-autoformat@^18.0.0" \
	"@ckeditor/ckeditor5-basic-styles@^18.0.0" \
	"@ckeditor/ckeditor5-block-quote@^18.0.0" \
	"@ckeditor/ckeditor5-ckfinder@^18.0.0" \
	"@ckeditor/ckeditor5-core@^18.0.0" \
	"@ckeditor/ckeditor5-easy-image@^18.0.0" \
	"@ckeditor/ckeditor5-essentials@^18.0.0" \
	"@ckeditor/ckeditor5-heading@^18.0.0" \
	"@ckeditor/ckeditor5-image@^18.0.0" \
	"@ckeditor/ckeditor5-indent@^18.0.0" \
	"@ckeditor/ckeditor5-link@^18.0.0" \
	"@ckeditor/ckeditor5-list@^18.0.0" \
	"@ckeditor/ckeditor5-media-embed@^18.0.0" \
	"@ckeditor/ckeditor5-paragraph@^18.0.0" \
	"@ckeditor/ckeditor5-paste-from-office@^18.0.0" \
	"@ckeditor/ckeditor5-table@^18.0.0" \
	"@ckeditor/ckeditor5-theme-lark@^18.0.0" \
	'@ckeditor/ckeditor5-editor-inline@^18.0.0' \
	'@ckeditor/ckeditor5-alignment@^18.0.0' \
	'@ckeditor/ckeditor5-font@^18.0.0' \
	'@ckeditor/ckeditor5-highlight@^18.0.0' \
	'@ckeditor/ckeditor5-upload@^18.0.0' \
	'@ckeditor/ckeditor5-remove-format@^18.0.0' \
	'@ckeditor/ckeditor5-editor-decoupled@^18.0.0' \
	"eslint" \
	"eslint-config-ckeditor5" \
	"husky" \
	"lint-staged" \
	"postcss-loader" \
	"raw-loader" \
	"style-loader" \
	"stylelint" \
	"stylelint-config-ckeditor5" \
	"terser-webpack-plugin" \
	"webpack" \
	"webpack-cli" \
	"@ckeditor/ckeditor5-dev-utils@^12.0.7" \
	"@ckeditor/ckeditor5-dev-webpack-plugin@^8.0.7" \
	'@wiris/mathtype-ckeditor5@^7.17.1';


yarn run build