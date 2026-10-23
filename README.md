<div align="center">
  <img src="docs/assets/jingyu-mark.svg" width="88" alt="�����ʶ" />
  <h1>���� �� Jingyu</h1>
  <p>��ࡢԭ����ע���Ķ������ Android DeepSeek �ͻ��ˡ�</p>
  <p><a href="#���ٿ�ʼ">���ٿ�ʼ</a> �� <a href="docs/BUILDING.md">����</a> �� <a href="docs/ARCHITECTURE.md">�ܹ�</a> �� <a href="CHANGELOG.md">������־</a> �� <a href="CONTRIBUTING.md">���빱��</a></p>
</div>

## ��Ŀ���

����ʹ�� Java �� Android ԭ���ؼ�������ͨ���û��Լ��� API Key ֱ�� DeepSeek����ǰ�汾 **2.2.1**��֧�� Android 8.0��API 26�������ϡ�

| ���� | ʵ�� |
| --- | --- |
| �Ի����Ķ� | ��ʽ�����Markdown��LaTeX�����������ǳɫ����ɫ���� |
| ������ | ������ʷ���ɱ༭���䡢���蹤�߹��򡢳��Ի����� |
| �ļ����� | �����Ķ���Word��PDF��Excel��Markdown��TXT��CSV ���� |
| ���ع��� | ��ѧ���㡢ͳ�ơ��๤������ԭ����״/����ͼ |
| ���ݹ��� | ��ʷ�ͼ��䱸�ݡ��ļ�������������˻�����ѯ |

PPT��HTML���������ִ�к����� Office ��ʽ���̲��ڵ�ǰ������Χ�ڡ���������ȡ���ڷ���˹��߽ӿڵĿ����ԡ�

## ���ٿ�ʼ

ʹ������֤�� APK ʱ����Ӧ�õġ��˻������ӡ�����д�Լ��� DeepSeek API Key��API ʹ�÷����ɷ����̼��գ���Ŀ���ṩ������Կ��

�����߿���ֱ���� Android Studio ����Ŀ��Ŀ¼��ѡ�� `app` ģ��� `debug` ������

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

Windows ʹ�� `gradlew.bat`����Ҫ JDK 17 �� Android SDK 35��Gradle Wrapper �̶�Ϊ 8.11.1��������ʹ�ö���Ӧ�� ID `cn.jingyu.deepseek.debug`�����Ḳ����ʽ�档

��������ʱ��������������֤�� PowerShell ��������·������ [�����ĵ�](docs/BUILDING.md)��Gradle ���̺� CI �����Ѳ��룬�����λ����޷����� Gradle/AGP����δ��� Gradle ·��ʵ�ܣ�������Ϊ CI ��ͨ����

## ��Ŀ�ṹ

| Ŀ¼ | ��; |
| --- | --- |
| `app/src/main/` | Ӧ��Դ�롢ԭ����Դ�ͱ������� |
| `app/src/test/` | JUnit �ع���� |
| `test/` | ���ġ��ļ������ʽ�߼����豸����Դ�� |
| `vendor/` | �̶��汾��������ӦԴ�� JAR |
| `docs/` | �������ܹ�����˽�������Ͳ���˵�� |
| `.github/` | CI������ģ�塢PR ģ��������������� |

## ��֤״̬

2.2.1 ���� **117 �������ع���**��95 �����/�ļ�/�����飬�� 22 ��֡���ȡ����׺�ԭ�ı߽��顣��ʾʱ������ͼ������֤�����߼������ܴ��� Android ʵ��֡�ʲ���������δ�������Ӿ������������գ���� [����˵��](docs/TESTING.md)��

## ��������˽

API Key ʹ�� Android Keystore ���ܱ��棻���ڼ���ʹ�ñ��ؼ��ܴ洢���Ự��ʷ��Ӧ��˽��Ŀ¼���� JSON ���棬���Ƕ˵��˼��ܡ�������Ϣ���������ݼ���ؼ�����ܳ�Ϊ����ģ�͵������ģ��ֶ������ı���Ϊ���ġ���� [��˽˵��](docs/PRIVACY.md)��

## ����֤������

��Ŀԭ��������� [Apache-2.0](LICENSE)�����������롢������屣����������֤����� [THIRD_PARTY.md](THIRD_PARTY.md)������Ŀ�Ƕ����ͻ��ˣ��� DeepSeek �ٷ�û��������ϵ��

**Crafted by [@zhaozh0321](https://github.com/zhaozh0321) �� with GPT**

