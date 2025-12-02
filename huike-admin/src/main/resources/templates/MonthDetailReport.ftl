<!DOCTYPE html>
<html>
<head>
    <style>
        body, html {
            height: 100%;
            margin: 0;
        }

        table {
        	margin: auto;
        	margin-top: 5px;
            width: 80%;
            border-collapse: collapse;
        }

		tr {
			line-height: 20px;
		}

		.tr_head {
			line-height: 50px;
		}

        th, td {
            padding: 10px;
            text-align: center;
            border: 1px solid black;
        }

        th {
        	background-color: #f9c46b;
        }

        td {
        	background-color: #e3e3e3;
        }

        p{
        	text-align: center;
        	font-size: 20px;
        }
    </style>
</head>
<body>

	<p>您好 , 超级管理员 , 以下是本月公司运营数据 , 请及时查收 , 数据有任何问题 , 请联系开发人员。<p>

    <table>
        <tr class="tr_head">
        	<th>日期</th>>
            <th>线索新增</th>
            <th>商机新增</th>
            <th>合同新增</th>
            <th>销售额</th>
        </tr>

        <#list itemList as item >
        <tr>
        	<td>${item.date}</td>
            <td>${item.newClueCount}</td>
            <td>${item.newBusinessCount}</td>
            <td>${item.newContractCount}</td>
            <td>${item.saleMoney}</td>
        </tr>
        </#list>

    </table>

    <br><br><br>
</body>
</html>
