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
  - 一時チャンネルの参加上限変更
- **一時通話への参加募集メッセージ送信**
  - リンク中一時通話チャンネル削除による、メッセージも自動削除
  - 募集内容をカスタム
- **リアクションロールの追加と管理**
  - 対象となるメッセージを設定、変更
  - 付与ロールと対象リアクションの設定、変更、削除
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
###### ※PREFIXを変更すると`>`が変更されますが
###### `>help`と`>show`はいつでも打てます
- [ADMIN]確認用コマンド一覧
  - `>help` : コマンド一覧を表示
  - `>show` : サーバーの設定状況を確認
- [ADMIN]設定コマンド一覧
  - `>setup` : 必要なチャンネルとカテゴリを自動作成
  - `>set prefix <1~100文字>` : コマンドの前に打つ文字を変更
  - `>set vcat <カテゴリID>` : 一時通話の作成先を変更
  - `>set tcat <カテゴリID>` : 一時チャットの作成先を変更
  - `>set 1stc <チャンネルID>` : 最初に入るチャンネルを変更
  - `>set men <チャンネルID>` : 募集送信チャンネル変更
  - `>set role <ロールID> <絵文字>` : リアクションロールの付与ロールと絵文字を変更
  - `>set enable <true or false>` -> 一時通話チャンネル作成切替
  - `>set text <true or false>` -> 一時テキストチャンネル作成切替
  - `>set size <0~99の数字>` -> 一時通話初期人数変更
  - `>set mess <メッセージID>　<チャンネルID>` : リアクションロールの対象メッセージを変更
  - `>remove role <絵文字>` : リアクションロールの絵文字を削除
- [ADMIN]募集テンプレ設定
  - `>set stereo <テンプレ内容>` : テンプレ内で使える置換！
    - `&user&` : 送信を選択したユーザーのメンションに置換
    - `&text&` : 募集コマンドの募集内容で入力した内容に置換
    - `&channel&` : 募集したい通話チャネルに置換
    - `&everyone&` : Everyoneメンションに置換
    - `&here&` : Hereメンションに置換
    - `/n` : 改行
- [ADMIN]ユーティリティ
  - `>mess <文字><画像>` -> メッセージをBOTに送信させなおす
- [USER]一時チャネルコマンド一覧
  - `>name <文字>`or`>n <文字>` : チャンネルの名前を変更
  - `>size <数字>`or`>s <数字>` : 通話参加人数を変更
  - `>men <募集内容>`or`>m <募集内容>` : 募集チャットの内容を書いて送信
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