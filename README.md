# Visitor
Программное средство для составления расписания контрольных и взаимных посещений преподавателями занятий. На основе готового расписания занятости преподавателей автоматически генерирует график посещений. Полученный график можно корректировать вручную.

На вход принимает пару файлов: файл **Excel** с занятостью преподавателей и текстовый файл (названный как и файл Excel (без расширения), но оканчивающийся на `_преподаватели.txt`).

------------

#### Файл занятости преподавателей
Образец файла Excel — `ext/template_schedule_plain.xls`
- Один лист на преподавателя (название листа — ФИО).
- Начиная со столбца №3 (**C**) заполняются даты и занятия.
- Занятия в строках, где указан временной интервал, даты — где не указан.
- Можно менять временные интервалы и увеличивать их количество.

Формат записи занятия должен удовлетворять следующему регулярному выражению (порядок групп — дисциплина, тип с номером занятия, аудитория, учебная группа):

```
([^:]+):?\s([а-яА-Я._]+\.?-[\d.]+)[,\s]\s?а(?:уд)?\.\s?([^\s]+)\s(?:гр\.\s)?(.+)
```

------------

#### Файл преподавателей
Формат текстового файла преподавателей следующий. Каждая строка должна иметь вид:
```
<Имя, как в файле расписаний>|<Должность>|<Должен ли посещать занятия
{1 - может,0 - не может}>|<Нужно ли его посещать {1 - нужно,0 - не нужно}>
|<подразделение (необязательно)>|<ученое звание, степень (необязательно)>
```

Примеры: `Иванов Иван Иванович|Доцент|0|1|кафедры`, `Петров Петр Петрович|профессор|0|1||к.ф.-м.н.`

_Последние два поля необязательные, нужны для подготовки листов контроля._

Список допустимых должностей (от старшего к младшему с точки зрения дополнительного правила на посещения): _Заведующий, Заместитель заведующего, Профессор, Доцент, Старший преподаватель, Преподаватель, Ассистент, Научный сотрудник, Аспирант._

------------

#### Файл фиксированных посещений
Каждая строка файла фиксированных посещений — одно посещение. Формат:
```
<Имя преподавателя, как в файле расписаний>,<Имя посещающего, как в файле расписаний>,
<Временной интервал | ЧЧ:ММ-ЧЧ:ММ>,<Дата | ГГГГ-ММ-ДД>
```
Можно также сохранить текущие фиксированные посещения из таблицы в файл для последующей загрузки.

------------

#### Указание нежелательных и недопустимых периодов времени для посещающих
Пока только в интерфейсе. Можно добавить разовый или повторяющийся временной интервал как нежелательный или недопустимый для посещающего.

Если в разовом указать только время, он считается ежедневным. Если в повторяющемся не указать временные рамки, будут использованы даты крайних по датам занятий из файла занятости.

------------

(c) 2018-2019, Дмитрий Мольков