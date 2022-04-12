# wkwkbot
***
[BOT追加](https://discord.com/oauth2/authorize?client_id=937343064811384924&scope=bot&permissions=8)  
[サポートDiscordサーバー](https://discord.gg/6Z7jabh983)

[![Discord Presence](https://lanyard.cnrad.dev/api/422099698388697108?idleMessage=今は何もしてないよ&hideTimestamp=true
)](https://discord.com/users/422099698388697108)
***
## BOT保有機能
- **一時通話チャンネルの管理**
  - 通話自動削除
  - 通話非表示切替
  - 通話参加許可切替
  - 一時チャンネルの名前変更
  - 一時チャンネルの名前テンプレート
  - 一時チャンネルの参加上限変更
- **一時通話への参加募集メッセージ送信**
  - 一時通話チャンネル削除による、メッセージ自動削除
  - 募集内容をカスタム
- **リアクションロールの追加と管理**
  - 対象となるメッセージを設定、変更
  - 付与ロールと対象リアクションの設定、変更、削除
  - 名前テンプレートの選択削除
- **サーバー毎の設定変更**
  - 一時通話作成の ON・OFF
  - 一時テキスト作成の ON・OFF
  - 新規一時チャンネル作成時の接続チャネル変更
  - 新規一時通話チャンネル作成先カテゴリの変更
  - 新規一時テキストチャンネル作成先カテゴリの変更
  - 一時通話初期人数変更
  - PREFIXの変更
- その他機能
  - サーバー情報取得
  - コマンドヘルプ機能
  - BOTによるメッセージ代替送信
***
### コマンド情報(デフォルト)
不具合やご不明な点がございましたら サポートサーバーに参加し
チャネルにお書きいただくか、管理者にDMを送ってください。

- 一時通話チャンネルの管理
  - 通話自動削除
  - 通話非表示切替
  - 通話参加許可切替
  - 一時チャンネルの名前変更
  - 一時チャンネルの名前テンプレート
  - 一時チャンネルの参加上限変更
- 一時通話への参加募集メッセージ送信
  - 一時通話チャンネル削除による、メッセージ自動削除
  - 募集内容をカスタム
- リアクションロールの追加と管理
  - 対象となるメッセージを設定、変更
  - 付与ロールと対象リアクションの設定、変更、削除
  - 名前テンプレートの選択削除
- サーバー毎の設定項目
  - 一時通話作成の ON・OFF
  - 一時テキスト作成の ON・OFF
  - 新規一時チャンネル作成時の接続チャネル変更
  - 新規一時通話チャンネル作成先カテゴリの変更
  - 新規一時テキストチャンネル作成先カテゴリの変更
  - 一時通話初期人数変更
- その他機能
  - ログ設定(ユーザー入退出、メッセージ削除ログ)
  - メッセージ自動削除(秒、分、時、日で時間指定)
  - サーバー情報取得
  - コマンドヘルプ機能
  - BOTによるメッセージ代替送信

***
##### セットアップ方法
###### ※以下は自分でBOTを動かしたい方向けとなっております
インストールするべきもの。  
[MySQL](https://dev.mysql.com/downloads/mysql/) ※インストールしてください  
[JDK-17](https://www.oracle.com/java/technologies/downloads/) ※パッチを通してください  
[Maven](https://maven.apache.org/download.cgi) ※パッチを通してください
1. BotToken,CLIENT ID,CLIENT SECRETを取得 [DEVELOPER PORTAL](https://discord.com/developers/applications)
2. 1 で取得した情報を追記し [DB.txt](https://github.com/wkwk-3/wkwkbot/blob/main/DB/DB.txt) をSQLで実行
3. プロジェクト内で`mvn install`を実行
4. targetフォルダ内で`java -jar <ファイル名>.jar`を実行
***
###### 今後追加する機能(予定)
- Webサイトからの管理機能
- 読み上げ機能
- 各機能の有効化切り替え
- その他要望の可能な限りの対応
***
###### 以下使用API
[MySQL Connecter / J](https://github.com/mysql/mysql-connector-j)  
[JavaCord](https://javacord.org)  
[emoji-java](https://github.com/vdurmont/emoji-java)  
[lombok](https://github.com/projectlombok/lombok)