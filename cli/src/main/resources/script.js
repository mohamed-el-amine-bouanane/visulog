let chartsSelector = document.querySelector('#charts-selector');
let moduleSelector = document.querySelector('#module-selector');

let chartObject;
let gitData;

document.querySelector('#header').innerText = `Visulog - Charts`;

fetch('data.json').then(res => res.json()).then(res => gitData = res).then(() => {
    for(let chart of gitData.map(m => m.options.charts).reduce((a, b) => [...a, ...b])) {
        let chartRadio = document.createElement('input');
        let chartLabel = document.createElement('label');
        chartRadio.name = 'chartType';
        chartLabel.innerText = chart;
        chartRadio.value = chart;
        chartRadio.type = 'radio';
        chartRadio.onclick = () => {
            displayCharts();
        }
        chartRadio.checked = chartsSelector.children.length == 0 ? true : false;
        chartsSelector.append(chartLabel);
        chartsSelector.append(chartRadio);
    }

    for(let module of gitData) {
        let moduleRadio = document.createElement('input');
        moduleRadio.type = 'radio';
        moduleRadio.name = 'modules';
        moduleRadio.value = module.id;
        moduleRadio.onclick = () => {
            displayCharts();
        }
        let moduleLabel = document.createElement('label');
        moduleLabel.innerText = module.options.valueOptions.displayName ? module.options.valueOptions.displayName : module.name;
        moduleSelector.append(moduleLabel);
        moduleSelector.append(moduleRadio);
    }
});

function displayCharts() {
    // document.getElementById('chart')
    if(chartObject) chartObject.destroy();
    let chartType;
    for(let chart of chartsSelector) {
        if(chart.checked) chartType = chart.value;
    }
    let module;
    for(let mod of moduleSelector) {
        if(mod.checked) module = gitData.find(m => m.id == mod.value);
    }
    if(Array.isArray(module.data)) {
        module.data = module.data.reduce((a, c) => {
            a[c.date] = c.added + c.deleted;
            return a;
        }, {});
    }
    let dataset = {
        label: module.options.valueOptions.displayName ? module.options.valueOptions.displayName : module.name,
        data: Object.values(module.data),
        backgroundColor: module.options.valueOptions.color ? module.options.valueOptions.color : Object.entries(module.data).map(a => randomColor()),
    };
    var ctx = document.getElementById('chart').getContext('2d');
    console.log(module.data);
    var chartConfig = {
        type: chartType,
        data: {
            labels: Object.keys(module.data),
            datasets: [dataset]
        }
    };
    chartObject = new Chart(ctx, chartConfig);
    if(module.options?.valueOptions?.width) {
        document.getElementById('chart').parentNode.style.width = module.options.valueOptions.width + 'px';
    }
}

function randomColor() {
    return `rgba(${Math.floor(Math.random()*255)}, ${Math.floor(Math.random()*255)}, ${Math.floor(Math.random()*255)}, ${Math.random()*0.5+0.5})`
}
